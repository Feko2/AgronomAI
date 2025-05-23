import React, { useState, useEffect } from 'react';
import { generateSampleData, addRecentReadings, fetchParcelas } from '../services/api';

const DataControls = ({ onParcelaChange, selectedParcela }) => {
  const [parcelas, setParcelas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    loadParcelas();
  }, []);

  const loadParcelas = async () => {
    try {
      const response = await fetchParcelas();
      setParcelas(response.data);
    } catch (error) {
      console.error('Error loading parcelas:', error);
    }
  };

  const handleGenerateData = async () => {
    setLoading(true);
    setMessage('');
    try {
      const response = await generateSampleData();
      setMessage(response.data.message);
      setTimeout(() => {
        loadParcelas(); // Recargar parcelas después de generar datos
        window.location.reload(); // Recargar la página para mostrar nuevos datos
      }, 1000);
    } catch (error) {
      setMessage('Error al generar datos de muestra');
      console.error('Error generating data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleAddRecentData = async () => {
    setLoading(true);
    setMessage('');
    try {
      const response = await addRecentReadings();
      setMessage(response.data.message);
      setTimeout(() => {
        window.location.reload(); // Recargar para mostrar nuevos datos
      }, 1000);
    } catch (error) {
      setMessage('Error al añadir lecturas recientes');
      console.error('Error adding recent data:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow p-6 mb-6">
      <h3 className="text-lg font-semibold text-gray-800 mb-4">Controles de Datos</h3>
      
      {/* Selección de Parcela */}
      <div className="mb-4">
        <label className="block text-sm font-medium text-gray-700 mb-2">
          Seleccionar Parcela
        </label>
        <select
          value={selectedParcela || ''}
          onChange={(e) => onParcelaChange(e.target.value || null)}
          className="w-full p-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="">Todas las parcelas (Vista general)</option>
          {parcelas.map((parcela) => (
            <option key={parcela.parcelaId} value={parcela.parcelaId}>
              {parcela.nombre} ({parcela.tipoCultivo})
            </option>
          ))}
        </select>
      </div>

      {/* Controles de Generación de Datos */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
        <button
          onClick={handleGenerateData}
          disabled={loading}
          className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {loading ? 'Generando...' : 'Generar Datos de Muestra'}
        </button>
        
        <button
          onClick={handleAddRecentData}
          disabled={loading}
          className="px-4 py-2 bg-green-500 text-white rounded-md hover:bg-green-600 disabled:bg-gray-400 disabled:cursor-not-allowed"
        >
          {loading ? 'Añadiendo...' : 'Añadir Lecturas Recientes'}
        </button>
      </div>

      {/* Información sobre los controles */}
      <div className="bg-gray-50 p-4 rounded-md text-sm text-gray-600 mb-4">
        <p><strong>Generar Datos de Muestra:</strong> Limpia la base de datos y crea 5 parcelas con ~100 lecturas cada una.</p>
        <p><strong>Añadir Lecturas Recientes:</strong> Agrega 1-3 lecturas nuevas a cada parcela existente.</p>
      </div>

      {/* Lista de Parcelas */}
      {parcelas.length > 0 && (
        <div>
          <h4 className="font-medium text-gray-700 mb-2">Parcelas Disponibles ({parcelas.length})</h4>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
            {parcelas.map((parcela) => (
              <div 
                key={parcela.parcelaId} 
                className={`p-3 border rounded-md cursor-pointer transition-colors ${
                  selectedParcela === parcela.parcelaId 
                    ? 'border-blue-500 bg-blue-50' 
                    : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => onParcelaChange(parcela.parcelaId)}
              >
                <div className="font-medium text-gray-800">{parcela.nombre}</div>
                <div className="text-sm text-gray-600">
                  {parcela.tipoCultivo} • {parcela.areaHectareas}ha • {parcela.ubicacion}
                </div>
                <div className="text-xs text-gray-500 mt-1">
                  Estado: {parcela.estado} • ID: {parcela.parcelaId}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Mensaje de Estado */}
      {message && (
        <div className={`mt-4 p-3 rounded-md ${
          message.includes('Error') 
            ? 'bg-red-100 text-red-700' 
            : 'bg-green-100 text-green-700'
        }`}>
          {message}
        </div>
      )}
    </div>
  );
};

export default DataControls; 