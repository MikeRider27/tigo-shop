import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { orderApi } from '../api/endpoints';
import { Alert, Spinner } from '../components/ui';
import { formatMoney } from '../utils/format';

export default function OrderConfirmationPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(useLocation().state?.order ?? null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!order) orderApi.get(id).then(setOrder).catch((err) => setError(err.message));
  }, [id, order]);

  if (error) return <Alert>{error}</Alert>;
  if (!order) return <Spinner />;

  return (
    <section className="confirmation card pad">
      <div className="confirmation-icon" aria-hidden="true">✓</div>
      <h1>¡Pedido confirmado!</h1>
      <p className="muted">Gracias por tu compra. Guarda tu número de orden para darle seguimiento.</p>
      <p className="order-number" aria-label="Número de orden">
        {order.orderNumber}
      </p>
      <div className="confirmation-details">
        <div className="summary-row">
          <span>Total</span>
          <strong>{formatMoney(order.total)}</strong>
        </div>
        <div className="summary-row">
          <span>Envío a</span>
          <span className="text-right">{order.shippingAddress}</span>
        </div>
      </div>
      <div className="actions center">
        <Link to={`/orders/${order.id}`} className="btn">Ver detalle de la orden</Link>
        <Link to="/" className="btn btn-outline">Seguir comprando</Link>
      </div>
    </section>
  );
}
