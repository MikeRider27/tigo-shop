// src/services/catalogService.js
import axios from "axios";
import { CATALOG_URL } from "./api";


const catalogApi = axios.create({
  baseURL: CATALOG_URL,
});

export const getCatalog = () => {
  const token = localStorage.getItem("token");

  return catalogApi.get("/articulos", {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
};
