import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../api/endpoints';
import { Alert, EmptyState, Spinner, StatusBadge } from '../components/ui';
import { formatDateTime, formatMoney } from '../utils/format';

export default function OrdersPage() {
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    orderApi.list().then(setOrders).catch((err) => setError(err.message));
  }, []);

  if (error) return <Alert>{error}</Alert>;
  if (!orders) return <Spinner />;
  if (orders.length === 0) {
    return (
      <EmptyState title="Aún no tienes órdenes">
        <Link to="/" className="btn">Ir al catálogo</Link>
      </EmptyState>
    );
  }

  return (
    <section>
      <h1>Mis órdenes</h1>
      <ul className="order-list">
        {orders.map((o) => (
          <li key={o.id}>
            <Link to={`/orders/${o.id}`} className="card order-row">
              <div>
                <strong className="mono">{o.orderNumber}</strong>
                <span className="muted small">{formatDateTime(o.createdAt)}</span>
              </div>
              <span className="muted small">{o.itemCount} artículo(s)</span>
              <StatusBadge status={o.status} />
              <strong>{formatMoney(o.total)}</strong>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
