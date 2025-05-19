// src/App.js
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import Login from "./components/auth/Login";
import Register from "./components/auth/Register";
import Home from "./components/Home";
import Catalog from "./components/Catalog";
import Layout from "./components/Layout";
import Cart from "./components/Cart";
import { CartProvider } from "./context/CartContext";


function App() {
  return (
    <CartProvider>
      <Router>
        <Routes>
          <Route path="/" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Rutas protegidas */}
          <Route element={<Layout />}>
            <Route path="/home" element={<Home />} />
            <Route path="/catalog" element={<Catalog />} />
            <Route path="/cart" element={<Cart />} />
            {/* Aquí luego agregás /cart y /orders */}
          </Route>
        </Routes>
      </Router>
    </CartProvider>
  );
}


export default App;
