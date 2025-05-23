import React, { useState, useEffect } from 'react';
import { fetchGeneralInsights, fetchParcelaInsights, exportReport } from '../services/api';

const InsightsPanel = ({ selectedParcelaId = null }) => {
  const [insights, setInsights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

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
    try {
      const response = await exportReport(parcelaId, 'json');
      
      // Generate AI Analysis
      const aiAnalysis = generateAIAnalysis(response.data.data);
      
      // Create comprehensive report with AI insights
      const fullReport = {
        metadata: {
          parcelaId: parcelaId,
          fechaGeneracion: new Date().toISOString(),
          tipoReporte: 'Análisis Completo con IA'
        },
        analisisIA: aiAnalysis,
        datosOriginales: response.data.data
      };
      
      const filename = `reporte-ia-${parcelaId}-${new Date().toISOString().split('T')[0]}.json`;
      const dataStr = JSON.stringify(fullReport, null, 2);
      const dataBlob = new Blob([dataStr], { type: 'application/json' });
      
      const url = URL.createObjectURL(dataBlob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      
      alert('✅ Reporte con análisis de IA generado exitosamente');
    } catch (err) {
      console.error('Error exporting report:', err);
      alert('Error al exportar reporte');
    }
  };

  const generateAIAnalysis = (data) => {
    if (!data || !data.lecturas || data.lecturas.length === 0) {
      return {
        resumen: 'No hay datos suficientes para realizar análisis',
        recomendaciones: ['Generar más datos de sensores'],
        estado: 'INSUFICIENTE'
      };
    }

    const lecturas = data.lecturas;
    const parcela = data.parcela;
    
    // Calcular promedios
    const promedios = {
      humedad: (lecturas.reduce((sum, l) => sum + l.humedad, 0) / lecturas.length).toFixed(2),
      ph: (lecturas.reduce((sum, l) => sum + l.ph, 0) / lecturas.length).toFixed(2),
      nitrogeno: (lecturas.reduce((sum, l) => sum + l.nitrogeno, 0) / lecturas.length).toFixed(2),
      temperatura: (lecturas.reduce((sum, l) => sum + l.temperatura, 0) / lecturas.length).toFixed(2)
    };

    // Análisis de tendencias (últimos 3 días vs anteriores)
    const fechaCorte = new Date();
    fechaCorte.setDate(fechaCorte.getDate() - 3);
    
    const lecturasRecientes = lecturas.filter(l => new Date(l.fecha) >= fechaCorte);
    const lecturasAnteriores = lecturas.filter(l => new Date(l.fecha) < fechaCorte);
    
    let tendencias = {};
    if (lecturasRecientes.length > 0 && lecturasAnteriores.length > 0) {
      const promediosRecientes = {
        humedad: lecturasRecientes.reduce((sum, l) => sum + l.humedad, 0) / lecturasRecientes.length,
        ph: lecturasRecientes.reduce((sum, l) => sum + l.ph, 0) / lecturasRecientes.length,
        nitrogeno: lecturasRecientes.reduce((sum, l) => sum + l.nitrogeno, 0) / lecturasRecientes.length,
        temperatura: lecturasRecientes.reduce((sum, l) => sum + l.temperatura, 0) / lecturasRecientes.length
      };
      
      const promediosAnteriores = {
        humedad: lecturasAnteriores.reduce((sum, l) => sum + l.humedad, 0) / lecturasAnteriores.length,
        ph: lecturasAnteriores.reduce((sum, l) => sum + l.ph, 0) / lecturasAnteriores.length,
        nitrogeno: lecturasAnteriores.reduce((sum, l) => sum + l.nitrogeno, 0) / lecturasAnteriores.length,
        temperatura: lecturasAnteriores.reduce((sum, l) => sum + l.temperatura, 0) / lecturasAnteriores.length
      };

      tendencias = {
        humedad: ((promediosRecientes.humedad - promediosAnteriores.humedad) / promediosAnteriores.humedad * 100).toFixed(1),
        ph: ((promediosRecientes.ph - promediosAnteriores.ph) / promediosAnteriores.ph * 100).toFixed(1),
        nitrogeno: ((promediosRecientes.nitrogeno - promediosAnteriores.nitrogeno) / promediosAnteriores.nitrogeno * 100).toFixed(1),
        temperatura: ((promediosRecientes.temperatura - promediosAnteriores.temperatura) / promediosAnteriores.temperatura * 100).toFixed(1)
      };
    }

    // Evaluación de condiciones según cultivo
    const evaluacion = evaluarCondicionesCultivo(parcela.tipoCultivo, promedios);
    
    // Generar recomendaciones inteligentes
    const recomendaciones = generarRecomendaciones(parcela.tipoCultivo, promedios, tendencias, evaluacion);
    
    return {
      resumen: `Análisis de ${lecturas.length} lecturas para parcela ${parcela.nombre} (${parcela.tipoCultivo})`,
      promedios: promedios,
      tendencias: tendencias,
      evaluacion: evaluacion,
      recomendaciones: recomendaciones,
      estado: evaluacion.estadoGeneral,
      fechaAnalisis: new Date().toISOString(),
      alertas: generarAlertas(promedios, tendencias)
    };
  };

  const evaluarCondicionesCultivo = (cultivo, promedios) => {
    // Rangos óptimos por cultivo
    const rangosOptimos = {
      'Maíz': { humedad: [60, 80], ph: [6.0, 7.0], nitrogeno: [120, 180], temperatura: [20, 30] },
      'Trigo': { humedad: [50, 70], ph: [6.5, 7.5], nitrogeno: [100, 150], temperatura: [15, 25] },
      'Soja': { humedad: [55, 75], ph: [6.0, 7.0], nitrogeno: [80, 120], temperatura: [18, 28] },
      'Arroz': { humedad: [80, 95], ph: [5.5, 6.5], nitrogeno: [150, 200], temperatura: [22, 32] }
    };

    const rangos = rangosOptimos[cultivo] || rangosOptimos['Maíz']; // Default a Maíz
    
    const evaluaciones = {
      humedad: evaluarParametro(parseFloat(promedios.humedad), rangos.humedad),
      ph: evaluarParametro(parseFloat(promedios.ph), rangos.ph),
      nitrogeno: evaluarParametro(parseFloat(promedios.nitrogeno), rangos.nitrogeno),
      temperatura: evaluarParametro(parseFloat(promedios.temperatura), rangos.temperatura)
    };

    // Calcular estado general
    const estados = Object.values(evaluaciones);
    const excelentes = estados.filter(e => e === 'EXCELENTE').length;
    const buenos = estados.filter(e => e === 'BUENO').length;
    const regulares = estados.filter(e => e === 'REGULAR').length;
    const criticos = estados.filter(e => e === 'CRITICO').length;

    let estadoGeneral;
    if (criticos > 0) estadoGeneral = 'CRITICO';
    else if (regulares > 1) estadoGeneral = 'REGULAR';
    else if (buenos >= 2) estadoGeneral = 'BUENO';
    else if (excelentes >= 3) estadoGeneral = 'EXCELENTE';
    else estadoGeneral = 'BUENO';

    return {
      ...evaluaciones,
      estadoGeneral,
      puntuacion: (excelentes * 4 + buenos * 3 + regulares * 2 + criticos * 1) / 4
    };
  };

  const evaluarParametro = (valor, rango) => {
    const [min, max] = rango;
    const centro = (min + max) / 2;
    const tolerancia = (max - min) * 0.1; // 10% de tolerancia

    if (valor >= min && valor <= max) {
      if (Math.abs(valor - centro) <= tolerancia) return 'EXCELENTE';
      return 'BUENO';
    } else if (valor < min) {
      const deficit = ((min - valor) / min) * 100;
      return deficit > 20 ? 'CRITICO' : 'REGULAR';
    } else {
      const exceso = ((valor - max) / max) * 100;
      return exceso > 20 ? 'CRITICO' : 'REGULAR';
    }
  };

  const generarRecomendaciones = (cultivo, promedios, tendencias, evaluacion) => {
    const recomendaciones = [];

    // Recomendaciones por humedad
    if (evaluacion.humedad === 'CRITICO') {
      if (parseFloat(promedios.humedad) < 50) {
        recomendaciones.push('🚨 URGENTE: Aumentar riego inmediatamente. Humedad crítica.');
      } else {
        recomendaciones.push('🚨 URGENTE: Reducir riego y mejorar drenaje. Exceso de humedad.');
      }
    } else if (evaluacion.humedad === 'REGULAR') {
      recomendaciones.push('⚠️ Ajustar programa de riego para optimizar humedad del suelo.');
    }

    // Recomendaciones por pH
    if (evaluacion.ph === 'CRITICO') {
      if (parseFloat(promedios.ph) < 6.0) {
        recomendaciones.push('🚨 Aplicar cal agrícola para corregir acidez del suelo.');
      } else {
        recomendaciones.push('🚨 Aplicar azufre o materia orgánica para reducir alcalinidad.');
      }
    }

    // Recomendaciones por nitrógeno
    if (evaluacion.nitrogeno === 'CRITICO') {
      if (parseFloat(promedios.nitrogeno) < 100) {
        recomendaciones.push('🚨 Aplicar fertilizante nitrogenado urgentemente.');
      } else {
        recomendaciones.push('🚨 Suspender fertilización nitrogenada temporalmente.');
      }
    }

    // Recomendaciones por temperatura
    if (evaluacion.temperatura === 'CRITICO') {
      if (parseFloat(promedios.temperatura) > 35) {
        recomendaciones.push('🚨 Implementar sombreado o riego por aspersión para reducir temperatura.');
      } else {
        recomendaciones.push('🚨 Considerar protección contra heladas o cultivos de cobertura.');
      }
    }

    // Recomendaciones por tendencias
    if (tendencias.humedad && Math.abs(parseFloat(tendencias.humedad)) > 10) {
      recomendaciones.push(`📈 Tendencia de humedad: ${tendencias.humedad}% - Monitorear de cerca.`);
    }

    // Recomendaciones generales por cultivo
    if (cultivo === 'Maíz' && evaluacion.estadoGeneral === 'EXCELENTE') {
      recomendaciones.push('✅ Condiciones óptimas para maíz. Continuar con manejo actual.');
    }

    if (recomendaciones.length === 0) {
      recomendaciones.push('✅ Condiciones dentro de rangos aceptables. Mantener monitoreo regular.');
    }

    return recomendaciones;
  };

  const generarAlertas = (promedios, tendencias) => {
    const alertas = [];

    // Alertas críticas
    if (parseFloat(promedios.humedad) < 30 || parseFloat(promedios.humedad) > 90) {
      alertas.push({ tipo: 'CRITICO', mensaje: 'Humedad en niveles críticos', parametro: 'humedad' });
    }

    if (parseFloat(promedios.ph) < 5.0 || parseFloat(promedios.ph) > 8.0) {
      alertas.push({ tipo: 'CRITICO', mensaje: 'pH fuera de rango seguro', parametro: 'ph' });
    }

    if (parseFloat(promedios.temperatura) < 5 || parseFloat(promedios.temperatura) > 40) {
      alertas.push({ tipo: 'CRITICO', mensaje: 'Temperatura extrema detectada', parametro: 'temperatura' });
    }

    // Alertas de tendencia
    if (tendencias.humedad && Math.abs(parseFloat(tendencias.humedad)) > 15) {
      alertas.push({ tipo: 'TENDENCIA', mensaje: `Cambio rápido en humedad: ${tendencias.humedad}%`, parametro: 'humedad' });
    }

    return alertas;
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
                    className="px-3 py-1 text-xs bg-agro-green-600 text-white rounded hover:bg-agro-green-700"
                  >
                    Exportar IA
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