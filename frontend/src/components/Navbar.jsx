import { useState } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { cart } = useCart();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const close = () => setOpen(false);

  const handleLogout = () => {
    logout();
    close();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner container">
        <Link to="/" className="brand" onClick={close}>
          <span className="brand-mark" aria-hidden="true">T</span>
          <span>Sports Shop</span>
        </Link>

        <button
          className="menu-toggle"
          aria-label="Abrir menú"
          aria-expanded={open}
          aria-controls="main-nav"
          onClick={() => setOpen((o) => !o)}
        >
          <span />
          <span />
          <span />
        </button>

        <nav id="main-nav" className={`nav-links ${open ? 'open' : ''}`}>
          <NavLink to="/" end onClick={close}>
            Catálogo
          </NavLink>
          {isAuthenticated ? (
            <>
              <NavLink to="/orders" onClick={close}>
                Mis órdenes
              </NavLink>
              <NavLink to="/profile" onClick={close}>
                Hola, {user.firstName}
              </NavLink>
              <button className="link-button" onClick={handleLogout}>
                Salir
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" onClick={close}>
                Iniciar sesión
              </NavLink>
              <NavLink to="/register" className="btn btn-small" onClick={close}>
                Crear cuenta
              </NavLink>
            </>
          )}
          <NavLink to="/cart" className="cart-link" onClick={close} aria-label={`Carrito, ${cart.totalItems} artículos`}>
            <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
              <path
                fill="currentColor"
                d="M7 18c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49A1 1 0 0 0 20 4H5.21l-.94-2H1zm16 16c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"
              />
            </svg>
            <span className="cart-label">Carrito</span>
            {cart.totalItems > 0 && <span className="cart-count">{cart.totalItems}</span>}
          </NavLink>
        </nav>
      </div>
    </header>
  );
}
