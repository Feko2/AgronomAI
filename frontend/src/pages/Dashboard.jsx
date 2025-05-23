import React, { useState, useEffect } from 'react';
import { fetchSensorData, fetchParcelas } from '../services/api';
import ParcelaCard from '../components/ParcelaCard';
import InsightsPanel from '../components/InsightsPanel';
import DataControls from '../components/DataControls';

export default function Dashboard() {
  const [sensors, setSensors] = useState([]);
  const [parcelas, setParcelas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('resumen');
  const [selectedParcela, setSelectedParcela] = useState('');

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      setLoading(true);
      const [sensorsResponse, parcelasResponse] = await Promise.all([
        fetchSensorData(),
        fetchParcelas()
      ]);
      
      console.log('Sensors loaded:', sensorsResponse.data?.length || 0);
      console.log('Parcelas loaded:', parcelasResponse.data?.length || 0);
      
      setSensors(sensorsResponse.data || []);
      setParcelas(parcelasResponse.data || []);
    } catch (err) {
      console.error('Error loading data:', err);
      setError('Error al cargar los datos del dashboard');
    } finally {
      setLoading(false);
    }
  };

  // Agrupar sensores por parcela
  const getSensorsByParcela = (parcelaId) => {
    return sensors.filter(sensor => sensor.parcelaId === parcelaId);
  };

  // Filtrar parcelas según selección
  const filteredParcelas = selectedParcela === '' || selectedParcela === 'all' 
    ? parcelas 
    : parcelas.filter(p => p.parcelaId === selectedParcela);

  const renderContent = () => {
    switch (activeTab) {
      case 'resumen':
        return (
          <div>
            {/* Filtro de Parcelas */}
            <div className="mb-6">
              <label className="block text-sm font-medium text-agro-dark-300 mb-2">
                Filtrar por parcela:
              </label>
              <select
                value={selectedParcela}
                onChange={(e) => setSelectedParcela(e.target.value)}
                className="bg-agro-dark-800 text-white border border-agro-dark-600 rounded-lg px-3 py-2 w-64"
              >
                <option value="">Todas las parcelas</option>
                {parcelas.map(parcela => (
                  <option key={parcela.parcelaId} value={parcela.parcelaId}>
                    {parcela.nombre} ({parcela.tipoCultivo})
                  </option>
                ))}
              </select>
            </div>

            {/* Grid de Parcelas */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredParcelas.map(parcela => (
                <ParcelaCard
                  key={parcela.parcelaId}
                  parcela={parcela}
                  sensorData={getSensorsByParcela(parcela.parcelaId)}
                />
              ))}
            </div>

            {filteredParcelas.length === 0 && (
              <div className="text-center py-12">
                <div className="text-gray-400 text-lg">
                  {parcelas.length === 0 ? 'No hay parcelas registradas' : 'No se encontraron parcelas'}
                </div>
                <p className="text-gray-500 mt-2">
                  {parcelas.length === 0 ? 'Genera datos de muestra para comenzar' : 'Ajusta tu filtro o selecciona otra parcela'}
                </p>
              </div>
            )}

            {/* Debug Info */}
            <div className="mt-8 p-4 bg-agro-dark-900 rounded-lg">
              <h4 className="text-white font-medium mb-2">Estado del Sistema:</h4>
              <div className="text-sm text-agro-dark-300 space-y-1">
                <p>• Parcelas en base de datos: {parcelas.length}</p>
                <p>• Lecturas de sensores: {sensors.length}</p>
                <p>• Filtro activo: {selectedParcela || 'Todas las parcelas'}</p>
                <p>• Parcelas mostradas: {filteredParcelas.length}</p>
              </div>
            </div>
          </div>
        );
      
      case 'insights':
        return <InsightsPanel />;
      
      case 'controles':
        return <DataControls onDataGenerated={loadDashboardData} />;
      
      default:
        return null;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-agro-dark-950 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-agro-green-400 mx-auto"></div>
            <p className="text-gray-400 mt-4">Cargando dashboard...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-agro-dark-950 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-400 text-xl mb-4">⚠️ Error</div>
            <p className="text-gray-400">{error}</p>
            <button
              onClick={loadDashboardData}
              className="mt-4 bg-agro-green-600 hover:bg-agro-green-700 text-white px-4 py-2 rounded-lg"
            >
              Reintentar
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-agro-dark-950">
      {/* Header */}
      <div className="bg-agro-dark-900 border-b border-agro-dark-700">
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold text-white">🌱 AgroApp</h1>
              <p className="text-agro-dark-300">Sistema integral de monitoreo agrícola con análisis por IA</p>
            </div>
            <div className="text-sm text-agro-dark-400">
              {parcelas.length} parcelas • {sensors.length} lecturas
            </div>
          </div>

          {/* Tabs */}
          <div className="mt-6">
            <nav className="flex space-x-4">
              {[
                { id: 'resumen', label: 'Resumen', icon: '📊' },
                { id: 'insights', label: 'Insights AI', icon: '🤖' },
                { id: 'controles', label: 'Controles', icon: '⚙️' }
              ].map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors ${
                    activeTab === tab.id
                      ? 'bg-agro-green-600 text-white'
                      : 'text-agro-dark-300 hover:text-white hover:bg-agro-dark-800'
                  }`}
                >
                  <span>{tab.icon}</span>
                  <span>{tab.label}</span>
                </button>
              ))}
            </nav>
          </div>
        </div>
      </div>

      {/* Content */}
      <div className="max-w-7xl mx-auto p-6">
        {renderContent()}
      </div>
    </div>
  );
} 