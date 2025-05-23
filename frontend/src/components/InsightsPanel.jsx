import React, { useState, useEffect } from 'react';
import { fetchGeneralInsights, fetchParcelaInsights, exportReport } from '../services/api';

const InsightsPanel = ({ selectedParcelaId = null }) => {
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [exportingReports, setExportingReports] = useState(new Set()); // Track which reports are being exported

  useEffect(() => {
    loadInsights();
  }, [selectedParcelaId]);

  const loadInsights = async () => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (selectedParcelaId) {
        response = await fetchParcelaInsights(selectedParcelaId);
        setInsights([response.data]); // Wrap single parcela insight in array
      } else {
        response = await fetchGeneralInsights();
        setInsights(response.data);
      }
    } catch (err) {
      console.error('Error loading insights:', err);
      setError('Error al cargar insights');
      setInsights([]);
    } finally {
      setLoading(false);
    }
  };

  const handleExportReport = async (parcelaId) => {
    // Check if this report is already being generated
    if (exportingReports.has(parcelaId)) {
      return;
    }

    // Add to exporting set
    setExportingReports(prev => new Set([...prev, parcelaId]));

    try {
      // Show immediate feedback
      showToast('🧠 Generando reporte con análisis de IA...', 'info');
      
      const response = await exportReport(parcelaId, 'markdown');
      
      if (response.data.error) {
        throw new Error(response.data.error);
      }

      // Obtener el contenido Markdown del backend
      const markdownContent = response.data.content;
      const filename = response.data.filename;
      
      if (!markdownContent) {
        throw new Error('No se pudo generar el contenido del reporte');
      }

      // Crear y descargar el archivo Markdown
      const dataBlob = new Blob([markdownContent], { type: 'text/markdown' });
      
      const url = URL.createObjectURL(dataBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      
      showToast('✅ Reporte agronómico generado exitosamente', 'success');
    } catch (err) {
      console.error('Error exporting report:', err);
      showToast(`❌ Error: ${err.message}`, 'error');
    } finally {
      // Remove from exporting set
      setExportingReports(prev => {
        const newSet = new Set(prev);
        newSet.delete(parcelaId);
        return newSet;
      });
    }
  };

  const showToast = (message, type) => {
    // Create toast notification
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm transition-all duration-300 ${
      type === 'success' ? 'bg-green-600 text-white' :
      type === 'error' ? 'bg-red-600 text-white' :
      'bg-blue-600 text-white'
    }`;
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    // Auto remove after 3 seconds (or 5 for loading)
    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transform = 'translateX(100%)';
      setTimeout(() => document.body.removeChild(toast), 300);
    }, type === 'info' ? 5000 : 3000);
  };

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'excelente':
      case 'normal':
        return 'text-green-600 bg-green-100';
      case 'bueno':
      case 'alerta':
        return 'text-yellow-600 bg-yellow-100';
      case 'regular':
        return 'text-orange-600 bg-orange-100';
      case 'critico':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  if (loading) {
    return (
      <div className="bg-agro-dark-900 rounded-lg shadow p-6">
        <div className="animate-pulse">
          <div className="h-4 bg-agro-dark-700 rounded w-1/4 mb-4"></div>
          <div className="space-y-3">
            <div className="h-3 bg-agro-dark-700 rounded"></div>
            <div className="h-3 bg-agro-dark-700 rounded w-5/6"></div>
            <div className="h-3 bg-agro-dark-700 rounded w-4/6"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-agro-dark-900 rounded-lg shadow p-6">
        <div className="text-red-400 text-center">
          <p>{error}</p>
          <button 
            onClick={loadInsights}
            className="mt-2 px-4 py-2 bg-agro-green-600 text-white rounded hover:bg-agro-green-700"
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Progress indicator when generating reports */}
      {exportingReports.size > 0 && (
        <div className="bg-blue-600 text-white p-3 rounded-lg flex items-center space-x-3 animate-pulse">
          <svg className="animate-spin h-5 w-5" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>
            🧠 Generando {exportingReports.size} reporte{exportingReports.size > 1 ? 's' : ''} con análisis de IA... 
            Esto puede tomar 5-10 segundos
          </span>
        </div>
      )}

      <div className="flex justify-between items-center">
        <h3 className="text-lg font-semibold text-white">
          {selectedParcelaId ? `Insights - ${selectedParcelaId}` : 'Insights Generales'}
        </h3>
        <button 
          onClick={loadInsights}
          className="px-3 py-1 text-sm bg-agro-dark-800 text-agro-dark-300 rounded hover:bg-agro-dark-700 hover:text-white"
        >
          Actualizar
        </button>
      </div>

      {insights.map((insight, index) => (
        <div key={index} className="bg-agro-dark-900 rounded-lg shadow p-6 border border-agro-dark-700">
          {/* Estadísticas Generales */}
          {insight.tipo === 'estadisticas_generales' && (
            <div>
              <h4 className="text-lg font-medium text-white mb-4">Resumen General</h4>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-400">{insight.totalParcelas}</div>
                  <div className="text-sm text-agro-dark-400">Parcelas</div>
                </div>
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-400">{insight.totalLecturas}</div>
                  <div className="text-sm text-agro-dark-400">Lecturas (7 días)</div>
                </div>
              </div>
              
              {insight.distribucionCultivos && (
                <div className="mb-4">
                  <h5 className="font-medium text-agro-dark-300 mb-2">Distribución por Cultivo</h5>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
                    {Object.entries(insight.distribucionCultivos).map(([cultivo, count]) => (
                      <div key={cultivo} className="bg-agro-dark-800 p-2 rounded text-sm">
                        <span className="font-medium text-white">{cultivo}:</span> <span className="text-agro-dark-300">{count}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Insights de Parcela Individual */}
          {insight.parcelaId && (
            <div>
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h4 className="text-lg font-medium text-white">{insight.nombre}</h4>
                  <p className="text-sm text-agro-dark-400">
                    {insight.cultivo} • ID: {insight.parcelaId}
                  </p>
                </div>
                <div className="flex items-center space-x-2">
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(insight.status)}`}>
                    {insight.status || 'Sin estado'}
                  </span>
                  <button
                    onClick={() => handleExportReport(insight.parcelaId)}
                    disabled={exportingReports.has(insight.parcelaId)}
                    className={`px-3 py-1 text-xs rounded flex items-center space-x-1 transition-all ${
                      exportingReports.has(insight.parcelaId)
                        ? 'bg-gray-500 text-white cursor-not-allowed'
                        : 'bg-agro-green-600 text-white hover:bg-agro-green-700'
                    }`}
                  >
                    {exportingReports.has(insight.parcelaId) ? (
                      <>
                        <svg className="animate-spin h-3 w-3 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        <span>Generando...</span>
                      </>
                    ) : (
                      <>
                        <svg className="h-3 w-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                        </svg>
                        <span>Exportar IA</span>
                      </>
                    )}
                  </button>
                </div>
              </div>

              {insight.promedios && (
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                  <div className="text-center">
                    <div className="text-lg font-semibold text-blue-400">{insight.promedios.humedad}%</div>
                    <div className="text-xs text-agro-dark-400">Humedad</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-semibold text-green-400">{insight.promedios.ph}</div>
                    <div className="text-xs text-agro-dark-400">pH</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-semibold text-purple-400">{insight.promedios.nitrogeno}</div>
                    <div className="text-xs text-agro-dark-400">Nitrógeno</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-semibold text-orange-400">{insight.promedios.temperatura}°C</div>
                    <div className="text-xs text-agro-dark-400">Temperatura</div>
                  </div>
                </div>
              )}

              {insight.ultimaLectura && (
                <div className="border-t border-agro-dark-700 pt-4">
                  <h5 className="font-medium text-agro-dark-300 mb-2">Última Lectura</h5>
                  <p className="text-sm text-agro-dark-400">
                    {new Date(insight.ultimaLectura.fecha).toLocaleString('es-ES')}
                  </p>
                  <div className="mt-2 grid grid-cols-2 md:grid-cols-4 gap-2 text-sm text-agro-dark-300">
                    <span>Humedad: {insight.ultimaLectura.humedad}%</span>
                    <span>pH: {insight.ultimaLectura.ph}</span>
                    <span>Nitrógeno: {insight.ultimaLectura.nitrogeno}</span>
                    <span>Temp: {insight.ultimaLectura.temperatura}°C</span>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      ))}

      {insights.length === 0 && (
        <div className="bg-agro-dark-900 rounded-lg shadow p-6 text-center text-agro-dark-400 border border-agro-dark-700">
          No hay insights disponibles
        </div>
      )}
    </div>
  );
};

export default InsightsPanel; 