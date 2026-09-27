import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { orderApi } from '../api/endpoints';
import { Alert, Spinner, StatusBadge } from '../components/ui';
import { ORDER_FLOW, ORDER_STATUS, formatDateTime, formatMoney } from '../utils/format';

function StatusTimeline({ status }) {
  if (status === 'CANCELADA') return <Alert tone="warning">Esta orden fue cancelada.</Alert>;
  const current = ORDER_FLOW.indexOf(status);
  return (
    <ol className="timeline" aria-label="Estado del pedido">
      {ORDER_FLOW.map((s, i) => (
        <li key={s} className={i <= current ? 'done' : ''} aria-current={i === current ? 'step' : undefined}>
          <span className="timeline-dot" aria-hidden="true" />
          {ORDER_STATUS[s].label}
        </li>
      ))}
    </ol>
  );
}

export default function OrderDetailPage() {
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [cancelling, setCancelling] = useState(false);

  const load = useCallback(() => orderApi.get(id).then(setOrder), [id]);

  useEffect(() => {
    load().catch((err) => setError(err.message));
  }, [load]);

  const cancel = async () => {
    if (!window.confirm('¿Seguro que deseas cancelar esta orden?')) return;
    setCancelling(true);
    setError('');
    try {
      setOrder(await orderApi.cancel(id));
    } catch (err) {
      setError(err.message);
      load().catch(() => {});
    } finally {
      setCancelling(false);
    }
  };

  if (!order) return error ? <Alert>{error}</Alert> : <Spinner />;

  return (
    <section>
      <Link to="/orders" className="back-link">← Mis órdenes</Link>
      <div className="page-header">
        <div>
          <h1 className="mono">{order.orderNumber}</h1>
          <p className="muted">Realizada el {formatDateTime(order.createdAt)}</p>
        </div>
        <StatusBadge status={order.status} />
      </div>
      <Alert>{error}</Alert>
      <StatusTimeline status={order.status} />

      <div className="layout-aside">
        <ul className="line-list card">
          {order.items.map((item) => (
            <li key={item.productId} className="line-item">
              <img src={item.imageUrl} alt="" width="72" height="72" />
              <div className="line-info">
                <strong>{item.name}</strong>
                <span className="muted small">
                  {item.quantity} × {formatMoney(item.unitPrice)}
                </span>
              </div>
              <strong className="line-subtotal">{formatMoney(item.subtotal)}</strong>
            </li>
          ))}
        </ul>
        <aside className="card summary">
          <h2>Resumen</h2>
          <div className="summary-row summary-total">
            <span>Total</span>
            <span>{formatMoney(order.total)}</span>
          </div>
          <h3>Dirección de envío</h3>
          <p className="address">{order.shippingAddress}</p>
          <p className="muted small">Última actualización: {formatDateTime(order.updatedAt)}</p>
          {order.status === 'CONFIRMADA' && (
            <button className="btn btn-danger btn-block" disabled={cancelling} onClick={cancel}>
              {cancelling ? 'Cancelando…' : 'Cancelar orden'}
            </button>
          )}
        </aside>
      </div>
    </section>
  );
}
