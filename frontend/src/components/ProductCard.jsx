import { formatMoney } from '../utils/format';

export default function ProductCard({ product, onAdd, busy }) {
  const soldOut = product.stock <= 0;
  return (
    <article className="card product-card">
      <img src={product.imageUrl} alt={product.name} loading="lazy" width="320" height="220" />
      <div className="product-body">
        <span className="product-category">{product.category}</span>
        <h3>{product.name}</h3>
        <p className="product-desc">{product.description}</p>
        <div className="product-meta">
          <strong className="price">{formatMoney(product.price)}</strong>
          <span className={`stock ${soldOut ? 'stock-out' : product.stock <= 5 ? 'stock-low' : ''}`}>
            {soldOut ? 'Agotado' : `${product.stock} disponibles`}
          </span>
        </div>
        <button className="btn btn-block" disabled={soldOut || busy} onClick={() => onAdd(product)}>
          {busy ? 'Agregando…' : soldOut ? 'Sin existencias' : 'Agregar al carrito'}
        </button>
      </div>
    </article>
  );
}
