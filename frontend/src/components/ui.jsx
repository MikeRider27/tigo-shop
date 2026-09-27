import { ORDER_STATUS } from '../utils/format';

export function Alert({ tone = 'danger', children }) {
  if (!children) return null;
  return (
    <div className={`alert alert-${tone}`} role={tone === 'danger' ? 'alert' : 'status'}>
      {children}
    </div>
  );
}

export function Field({ label, name, error, hint, as = 'input', ...props }) {
  const Tag = as;
  const id = `f-${name}`;
  return (
    <div className="field">
      <label htmlFor={id}>{label}</label>
      <Tag
        id={id}
        name={name}
        aria-invalid={Boolean(error)}
        aria-describedby={error ? `${id}-err` : hint ? `${id}-hint` : undefined}
        {...props}
      />
      {error ? (
        <span id={`${id}-err`} className="field-error">
          {error}
        </span>
      ) : (
        hint && (
          <span id={`${id}-hint`} className="field-hint">
            {hint}
          </span>
        )
      )}
    </div>
  );
}

export function Spinner({ label = 'Cargando…' }) {
  return (
    <div className="spinner" role="status">
      <span className="spinner-dot" aria-hidden="true" />
      {label}
    </div>
  );
}

export function StatusBadge({ status }) {
  const s = ORDER_STATUS[status] ?? { label: status, tone: 'info' };
  return <span className={`badge badge-${s.tone}`}>{s.label}</span>;
}

export function EmptyState({ title, children }) {
  return (
    <div className="empty">
      <h2>{title}</h2>
      {children}
    </div>
  );
}
