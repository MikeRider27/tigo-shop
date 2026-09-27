import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Alert, EmptyState, Spinner } from '../components/ui';
import { useCart } from '../context/CartContext';
import { formatMoney } from '../utils/format';

const MAX_QTY = 20;

export default function CartPage() {
  const { cart, loaded, update, remove, refresh } = useCart();
  const [error, setError] = useState('');
  const [busyId, setBusyId] = useState(null);

  useEffect(() => {
    refresh().catch((err) => setError(err.message));
  }, [refresh]);

  const run = async (productId, action) => {
    setError('');
    setBusyId(productId);
    try {
      await action();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyId(null);
    }
  };

  if (!loaded) return <Spinner />;

  if (cart.items.length === 0) {
    return (
      <EmptyState title="Tu carrito está vacío">
        <p className="muted">Explora el catálogo y agrega los artículos que te gusten.</p>
        <Link to="/" className="btn">Ir al catálogo</Link>
      </EmptyState>
    );
  }

  return (
    <section>
      <h1>Carrito de compras</h1>
      <Alert>{error}</Alert>
      <div className="layout-aside">
        <ul className="line-list card">
          {cart.items.map((item) => (
            <li key={item.productId} className="line-item">
              <img src={item.imageUrl} alt="" width="72" height="72" />
              <div className="line-info">
                <strong>{item.name}</strong>
                <span className="muted small">{formatMoney(item.unitPrice)} c/u</span>
              </div>
              <div className="qty" aria-label={`Cantidad de ${item.name}`}>
                <button
                  aria-label="Disminuir cantidad"
                  disabled={busyId === item.productId || item.quantity <= 1}
                  onClick={() => run(item.productId, () => update(item.productId, item.quantity - 1))}
                >
                  −
                </button>
                <span aria-live="polite">{item.quantity}</span>
                <button
                  aria-label="Aumentar cantidad"
                  disabled={busyId === item.productId || item.quantity >= MAX_QTY}
                  onClick={() => run(item.productId, () => update(item.productId, item.quantity + 1))}
                >
                  +
                </button>
              </div>
              <strong className="line-subtotal">{formatMoney(item.subtotal)}</strong>
              <button
                className="link-button danger"
                disabled={busyId === item.productId}
                onClick={() => run(item.productId, () => remove(item.productId))}
              >
                Eliminar
              </button>
            </li>
          ))}
        </ul>

        <aside className="card summary">
          <h2>Resumen</h2>
          <div className="summary-row">
            <span>Artículos</span>
            <span>{cart.totalItems}</span>
          </div>
          <div className="summary-row summary-total">
            <span>Total</span>
            <span>{formatMoney(cart.total)}</span>
          </div>
          <Link to="/checkout" className="btn btn-block">
            Continuar con el pedido
          </Link>
          <Link to="/" className="btn btn-outline btn-block">
            Seguir comprando
          </Link>
        </aside>
      </div>
    </section>
  );
}
