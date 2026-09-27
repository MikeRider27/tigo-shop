import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { orderApi, userApi } from '../api/endpoints';
import { Alert, Field, Spinner } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { formatMoney } from '../utils/format';

export default function CheckoutPage() {
  const { user, setUser } = useAuth();
  const { cart, loaded, reset, refresh } = useCart();
  const navigate = useNavigate();
  const [address, setAddress] = useState(user.shippingAddress);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(user.shippingAddress);
  const [saveAsDefault, setSaveAsDefault] = useState(false);
  const [draftError, setDraftError] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!loaded) return <Spinner />;
  if (cart.items.length === 0 && !submitting) return <Navigate to="/cart" replace />;

  const applyAddress = () => {
    if (!draft.trim()) {
      setDraftError('La dirección de envío es obligatoria');
      return;
    }
    if (draft.trim().length > 255) {
      setDraftError('La dirección no puede superar 255 caracteres');
      return;
    }
    setAddress(draft.trim());
    setEditing(false);
    setDraftError('');
  };

  const confirm = async () => {
    setError('');
    setSubmitting(true); // evita doble envío del pedido
    try {
      if (saveAsDefault && address !== user.shippingAddress) {
        const { firstName, lastName, birthDate } = user;
        setUser(await userApi.update({ firstName, lastName, birthDate, shippingAddress: address }));
      }
      const order = await orderApi.checkout(address);
      reset();
      navigate(`/orders/${order.id}/confirmation`, { replace: true, state: { order } });
    } catch (err) {
      setError(err.message);
      setSubmitting(false);
      refresh().catch(() => {});
    }
  };

  return (
    <section>
      <h1>Confirmar pedido</h1>
      <Alert>{error}</Alert>
      <div className="layout-aside">
        <div className="stack">
          <div className="card pad">
            <div className="section-title">
              <h2>Dirección de envío</h2>
              {!editing && (
                <button className="link-button" onClick={() => { setDraft(address); setEditing(true); }}>
                  Editar
                </button>
              )}
            </div>
            {editing ? (
              <>
                <Field label="Nueva dirección" name="address" as="textarea" rows={3} value={draft}
                       error={draftError} onChange={(e) => setDraft(e.target.value)} />
                <label className="checkbox">
                  <input type="checkbox" checked={saveAsDefault} onChange={(e) => setSaveAsDefault(e.target.checked)} />
                  Guardar como mi dirección predeterminada
                </label>
                <div className="actions">
                  <button className="btn btn-small" onClick={applyAddress}>Usar esta dirección</button>
                  <button className="btn btn-small btn-outline" onClick={() => { setEditing(false); setDraftError(''); }}>
                    Cancelar
                  </button>
                </div>
              </>
            ) : (
              <p className="address">
                {user.firstName} {user.lastName}
                <br />
                {address}
              </p>
            )}
          </div>

          <div className="card pad">
            <h2>Artículos</h2>
            <ul className="plain-list">
              {cart.items.map((i) => (
                <li key={i.productId} className="summary-row">
                  <span>
                    {i.quantity} × {i.name}
                  </span>
                  <span>{formatMoney(i.subtotal)}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <aside className="card summary">
          <h2>Total a pagar</h2>
          <div className="summary-row summary-total">
            <span>Total</span>
            <span>{formatMoney(cart.total)}</span>
          </div>
          <p className="muted small">Los precios y existencias se validan al confirmar.</p>
          <button className="btn btn-block" disabled={submitting || editing} onClick={confirm}>
            {submitting ? 'Confirmando…' : 'Confirmar pedido'}
          </button>
          <Link to="/cart" className="btn btn-outline btn-block">
            Volver al carrito
          </Link>
        </aside>
      </div>
    </section>
  );
}
