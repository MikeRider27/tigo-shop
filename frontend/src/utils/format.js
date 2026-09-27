const money = new Intl.NumberFormat('es-GT', { style: 'currency', currency: 'USD' });
const dateTime = new Intl.DateTimeFormat('es-GT', { dateStyle: 'medium', timeStyle: 'short' });

export const formatMoney = (value) => money.format(Number(value ?? 0));
export const formatDateTime = (iso) => dateTime.format(new Date(iso));

export const ORDER_STATUS = {
  CONFIRMADA: { label: 'Confirmada', tone: 'info' },
  EN_PREPARACION: { label: 'En preparación', tone: 'warning' },
  ENVIADA: { label: 'Enviada', tone: 'accent' },
  ENTREGADA: { label: 'Entregada', tone: 'success' },
  CANCELADA: { label: 'Cancelada', tone: 'danger' },
};

export const ORDER_FLOW = ['CONFIRMADA', 'EN_PREPARACION', 'ENVIADA', 'ENTREGADA'];
