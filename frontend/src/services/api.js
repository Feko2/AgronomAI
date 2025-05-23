import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

// Sensores
export const fetchSensorData = () => axios.get(`${API_BASE}/sensors`);

// Parcelas
export const fetchParcelas = () => axios.get(`${API_BASE}/parcelas`);
export const fetchParcelasActivas = () => axios.get(`${API_BASE}/parcelas/activas`);
export const fetchParcelaById = (id) => axios.get(`${API_BASE}/parcelas/${id}`);
export const fetchParcelasByCultivo = (cultivo) => axios.get(`${API_BASE}/parcelas/cultivo/${cultivo}`);

// Insights
export const fetchGeneralInsights = () => axios.get(`${API_BASE}/insights`);
export const fetchParcelaInsights = (parcelaId) => axios.get(`${API_BASE}/insights/parcela/${parcelaId}`);
export const fetchDetailedReport = (parcelaId) => axios.get(`${API_BASE}/insights/report/${parcelaId}`);
export const exportReport = (parcelaId, format = 'json') => axios.get(`${API_BASE}/insights/export/${parcelaId}?format=${format}`);

// Generador de datos
export const generateSampleData = () => axios.post(`${API_BASE}/data-generator/generate-sample`);
export const addRecentReadings = () => axios.post(`${API_BASE}/data-generator/add-recent`); 