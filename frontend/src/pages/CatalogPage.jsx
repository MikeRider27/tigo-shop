import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { catalogApi } from '../api/endpoints';
import ProductCard from '../components/ProductCard';
import { Alert, EmptyState, Spinner } from '../components/ui';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

const PAGE_SIZE = 12;

export default function CatalogPage() {
  const { isAuthenticated } = useAuth();
  const { add } = useCart();
  const navigate = useNavigate();
  // Los filtros viven en la URL: se pueden compartir y sobreviven al refresh / botón atrás.
  const [params, setParams] = useSearchParams();
  const q = params.get('q') ?? '';
  const category = params.get('category') ?? '';
  const page = Number(params.get('page') ?? 0);

  const [search, setSearch] = useState(q);
  const [categories, setCategories] = useState([]);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState(null);
  const [addingId, setAddingId] = useState(null);

  // Se parte siempre de los parámetros más recientes (ref), no de los capturados en el closure:
  // así el debounce de la búsqueda no pisa una categoría elegida mientras tanto.
  const paramsRef = useRef(params);
  paramsRef.current = params;
  const updateParams = (changes) => {
    const next = new URLSearchParams(paramsRef.current);
    Object.entries(changes).forEach(([k, v]) => (v === '' || v === 0 ? next.delete(k) : next.set(k, v)));
    paramsRef.current = next;
    setParams(next, { replace: true });
  };

  useEffect(() => {
    catalogApi.categories().then(setCategories).catch(() => setCategories([]));
  }, []);

  // Búsqueda con debounce de 350 ms mientras el usuario escribe.
  useEffect(() => {
    if (search === q) return undefined;
    const t = setTimeout(() => updateParams({ q: search.trim(), page: 0 }), 350);
    return () => clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search]);

  useEffect(() => {
    let cancelled = false;
    setError('');
    catalogApi
      .search({ q, category, page, size: PAGE_SIZE })
      .then((data) => !cancelled && setResult(data))
      .catch((err) => !cancelled && setError(err.message));
    return () => {
      cancelled = true;
    };
  }, [q, category, page]);

  const handleAdd = async (product) => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/' } });
      return;
    }
    setAddingId(product.id);
    setNotice(null);
    try {
      await add(product.id, 1);
      setNotice({ tone: 'success', text: `"${product.name}" se agregó al carrito.` });
    } catch (err) {
      setNotice({ tone: 'danger', text: err.message });
    } finally {
      setAddingId(null);
    }
  };

  return (
    <section>
      <div className="page-header">
        <div>
          <h1>Artículos deportivos</h1>
          <p className="muted">Encuentra el equipo ideal para tu deporte.</p>
        </div>
        <input
          className="search"
          type="search"
          placeholder="Buscar artículos…"
          aria-label="Buscar artículos"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <div className="chips" role="group" aria-label="Filtrar por categoría">
        {['', ...categories].map((c) => (
          <button
            key={c || 'all'}
            className={`chip ${category === c ? 'chip-active' : ''}`}
            aria-pressed={category === c}
            onClick={() => updateParams({ category: c, page: 0 })}
          >
            {c || 'Todas'}
          </button>
        ))}
      </div>

      {notice && (
        <Alert tone={notice.tone}>
          {notice.text} {notice.tone === 'success' && <Link to="/cart">Ver carrito</Link>}
        </Alert>
      )}
      <Alert>{error}</Alert>

      {!result && !error && <Spinner />}
      {result && result.content.length === 0 && (
        <EmptyState title="No encontramos artículos">
          <p className="muted">Prueba con otra búsqueda o categoría.</p>
        </EmptyState>
      )}
      {result && result.content.length > 0 && (
        <>
          <p className="muted small">{result.totalElements} artículos</p>
          <div className="product-grid">
            {result.content.map((p) => (
              <ProductCard key={p.id} product={p} onAdd={handleAdd} busy={addingId === p.id} />
            ))}
          </div>
          {result.totalPages > 1 && (
            <nav className="pagination" aria-label="Paginación">
              <button className="btn btn-outline" disabled={page === 0} onClick={() => updateParams({ page: page - 1 })}>
                Anterior
              </button>
              <span>
                Página {page + 1} de {result.totalPages}
              </span>
              <button
                className="btn btn-outline"
                disabled={page + 1 >= result.totalPages}
                onClick={() => updateParams({ page: page + 1 })}
              >
                Siguiente
              </button>
            </nav>
          )}
        </>
      )}
    </section>
  );
}
