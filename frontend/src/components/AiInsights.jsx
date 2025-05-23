import { useState } from 'react';
import axios from 'axios';

export default function AiInsights({ sensors }) {
  const [loading, setLoading] = useState(false);
  const [showConfig, setShowConfig] = useState(false);
  const [insights, setInsights] = useState([
    {
      id: 1,
      title: 'Nivel de pH óptimo',
      description: 'Los niveles de pH están dentro del rango óptimo para el cultivo actual. Mantenga las prácticas actuales de riego.',
      type: 'success',
      parcela: 'PARCELA-001',
    },
    {
      id: 2,
      title: 'Alerta de humedad',
      description: 'Se detecta una disminución en los niveles de humedad. Considere aumentar el riego en un 15% durante los próximos 3 días.',
      type: 'warning',
      parcela: 'PARCELA-002',
    },
    {
      id: 3,
      title: 'Bajo nivel de nitrógeno',
      description: 'Los niveles de nitrógeno están por debajo de lo óptimo. Se recomienda aplicar fertilizante rico en nitrógeno dentro de los próximos 5 días.',
      type: 'alert',
      parcela: 'PARCELA-001',
    },
  ]);
  
  // Function to generate new insights using backend API
  const generateInsights = async () => {
    setLoading(true);
    
    try {
      // Prepare the sensor data
      const sensorData = sensors.map(s => ({
        id: s.id,
        parcela: s.parcelaId,
        humedad: s.humedad,
        nitrogeno: s.nitrogeno,
        ph: s.ph,
        fecha: s.fecha
      }));
      
      // Call backend endpoint instead of OpenAI directly
      const response = await axios.post(
        'http://localhost:8080/api/insights/analyze',
        { sensorData },
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );
      
      if (response.data) {
        // Add IDs to the insights
        const updatedInsights = response.data.map((insight, index) => ({
          id: Date.now() + index,
          ...insight
        }));
        
        setInsights(updatedInsights);
      }
    } catch (error) {
      console.error('Error generating insights:', error);
      alert('Error al generar análisis. Por favor intente nuevamente.');
    } finally {
      setLoading(false);
    }
  };
  
  // Icon based on insight type
  const getIcon = (type) => {
    switch (type) {
      case 'success':
        return (
          <div className="p-1 rounded-full bg-green-900/20">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
        );
      case 'warning':
        return (
          <div className="p-1 rounded-full bg-yellow-900/20">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
        );
      case 'alert':
        return (
          <div className="p-1 rounded-full bg-red-900/20">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        );
      default:
        return (
          <div className="p-1 rounded-full bg-blue-900/20">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
        );
    }
  };

  return (
    <div className="bg-agro-dark-900 rounded-xl shadow p-4 h-full">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-medium text-white flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2 text-agro-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
          </svg>
          IA Insights
        </h3>
        <div className="flex gap-2">
          <button
            onClick={generateInsights}
            disabled={loading}
            className="px-3 py-1.5 text-xs font-medium rounded-lg bg-agro-green-700/30 text-agro-green-400 hover:bg-agro-green-700/50 transition-colors disabled:opacity-50"
          >
            {loading ? 'Analizando...' : 'Generar Análisis'}
          </button>
        </div>
      </div>

      {sensors.length === 0 ? (
        <div className="text-center py-10 text-agro-dark-400 italic">
          No hay datos para analizar.
        </div>
      ) : (
        <div className="space-y-3">
          {insights.map(insight => (
            <div key={insight.id} className="bg-agro-dark-950 p-3 rounded-lg border border-agro-dark-800">
              <div className="flex items-start">
                <div className="mr-3 mt-1">
                  {getIcon(insight.type)}
                </div>
                <div>
                  <div className="flex items-center">
                    <h4 className="text-sm font-medium text-white">{insight.title}</h4>
                    <span className="ml-2 text-xs px-2 py-0.5 rounded-full bg-agro-dark-700 text-agro-dark-300">
                      {insight.parcela}
                    </span>
                  </div>
                  <p className="mt-1 text-xs text-agro-dark-300">
                    {insight.description}
                  </p>
                </div>
              </div>
            </div>
          ))}
          
          <div className="pt-2">
            <button className="w-full py-2 text-xs font-medium text-agro-dark-400 hover:text-agro-green-400 transition-colors">
              Ver análisis completo →
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 