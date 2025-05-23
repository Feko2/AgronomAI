import React from 'react';

const ParcelaCard = ({ parcela, sensorData = [] }) => {
  // Calcular estadísticas de la parcela
  const calculateStats = () => {
    if (sensorData.length === 0) {
      return {
        avgHumedad: 0,
        avgNitrogeno: 0,
        avgPh: 0,
        avgTemperatura: 0,
        lastReading: null,
        totalReadings: 0,
        status: 'Sin datos'
      };
    }

    // Datos ordenados por fecha (más reciente primero)
    const sortedData = sensorData.sort((a, b) => new Date(b.fecha) - new Date(a.fecha));
    
    // Promedios
    const avgHumedad = sensorData.reduce((sum, s) => sum + (s.humedad || 0), 0) / sensorData.length;
    const avgNitrogeno = sensorData.reduce((sum, s) => sum + (s.nitrogeno || 0), 0) / sensorData.length;
    const avgPh = sensorData.reduce((sum, s) => sum + (s.ph || 0), 0) / sensorData.length;
    const avgTemperatura = sensorData.reduce((sum, s) => sum + (s.temperatura || 0), 0) / sensorData.length;

    // Calcular tendencia (últimos 3 días vs anteriores)
    const now = new Date();
    const threeDaysAgo = new Date(now.getTime() - (3 * 24 * 60 * 60 * 1000));
    
    const recentData = sortedData.filter(s => new Date(s.fecha) >= threeDaysAgo);
    const olderData = sortedData.filter(s => new Date(s.fecha) < threeDaysAgo);

    let trend = null;
    if (recentData.length > 0 && olderData.length > 0) {
      const recentAvgHumedad = recentData.reduce((sum, s) => sum + (s.humedad || 0), 0) / recentData.length;
      const olderAvgHumedad = olderData.reduce((sum, s) => sum + (s.humedad || 0), 0) / olderData.length;
      const change = ((recentAvgHumedad - olderAvgHumedad) / olderAvgHumedad) * 100;
      trend = {
        direction: change > 0 ? 'up' : change < 0 ? 'down' : 'stable',
        percentage: Math.abs(change).toFixed(1)
      };
    }

    // Determinar estado general
    const criticalCount = sensorData.filter(s => s.estado === 'CRITICO').length;
    const alertCount = sensorData.filter(s => s.estado === 'ALERTA').length;
    
    let status = 'EXCELENTE';
    if (criticalCount > sensorData.length * 0.3) status = 'CRITICO';
    else if (alertCount > sensorData.length * 0.5) status = 'ALERTA';
    else if (alertCount > 0 || criticalCount > 0) status = 'BUENO';

    return {
      avgHumedad: Math.round(avgHumedad * 10) / 10,
      avgNitrogeno: Math.round(avgNitrogeno * 10) / 10,
      avgPh: Math.round(avgPh * 100) / 100,
      avgTemperatura: Math.round(avgTemperatura * 10) / 10,
      lastReading: sortedData[0],
      totalReadings: sensorData.length,
      status,
      trend
    };
  };

  const stats = calculateStats();

  const getStatusColor = (status) => {
    switch (status) {
      case 'EXCELENTE': return 'text-green-600 bg-green-100';
      case 'BUENO': return 'text-blue-600 bg-blue-100';
      case 'ALERTA': return 'text-yellow-600 bg-yellow-100';
      case 'CRITICO': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getTrendIcon = (direction) => {
    switch (direction) {
      case 'up': return '📈';
      case 'down': return '📉';
      case 'stable': return '➡️';
      default: return '❓';
    }
  };

  return (
    <div className="bg-agro-dark-900 rounded-xl shadow p-4 border border-agro-dark-700">
      {/* Header */}
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-lg font-medium text-white">{parcela?.nombre || `Parcela ${parcela?.parcelaId}`}</h3>
          <p className="text-sm text-agro-dark-300">
            {parcela?.tipoCultivo} • {parcela?.areaHectareas}ha • {parcela?.ubicacion}
          </p>
        </div>
        <div className="flex items-center space-x-2">
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(stats.status)}`}>
            {stats.status}
          </span>
          {stats.trend && (
            <span className="text-xs text-agro-dark-300 flex items-center">
              {getTrendIcon(stats.trend.direction)} {stats.trend.percentage}%
            </span>
          )}
        </div>
      </div>

      {/* Métricas principales */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
        <div className="text-center">
          <div className="text-lg font-semibold text-blue-400">{stats.avgHumedad}%</div>
          <div className="text-xs text-agro-dark-400">Humedad</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-semibold text-green-400">{stats.avgNitrogeno}</div>
          <div className="text-xs text-agro-dark-400">Nitrógeno</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-semibold text-yellow-400">{stats.avgPh}</div>
          <div className="text-xs text-agro-dark-400">pH</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-semibold text-orange-400">{stats.avgTemperatura}°C</div>
          <div className="text-xs text-agro-dark-400">Temperatura</div>
        </div>
      </div>

      {/* Información adicional */}
      <div className="border-t border-agro-dark-700 pt-3">
        <div className="flex justify-between items-center text-sm">
          <span className="text-agro-dark-300">
            {stats.totalReadings} lecturas
          </span>
          {stats.lastReading && (
            <span className="text-agro-dark-400">
              Última: {new Date(stats.lastReading.fecha).toLocaleDateString('es-ES')}
            </span>
          )}
        </div>
        
        {stats.lastReading && (
          <div className="mt-2 text-xs text-agro-dark-400">
            <span>Última lectura: </span>
            <span>H: {stats.lastReading.humedad}% • </span>
            <span>N: {stats.lastReading.nitrogeno} • </span>
            <span>pH: {stats.lastReading.ph} • </span>
            <span>T: {stats.lastReading.temperatura}°C</span>
          </div>
        )}
      </div>
    </div>
  );
};

export default ParcelaCard; 