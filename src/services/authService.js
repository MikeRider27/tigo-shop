import axios from "axios";
import { AUTH_URL } from "./api";

const authApi = axios.create({
  baseURL: AUTH_URL,
});

// Login
export const login = (credentials) => authApi.post("/auth/login", credentials);

// Register
export const register = (data) => authApi.post("/auth/register", data);
