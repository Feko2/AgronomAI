import { useEffect, useState } from 'react';
import { fetchSensorData } from '../services/api';

// Components
import KpiCard from '../components/KpiCard';
import SensorCard from '../components/SensorCard';
import AiInsights from '../components/AiInsights';

export default function Dashboard() {
  const [sensors, setSensors] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('all');

  useEffect(() => {
    setLoading(true);
    fetchSensorData()
      .then(res => {
        console.log('API Response:', res);
        console.log('Data received:', res.data);
        setSensors(res.data || []);
        setLoading(false);
      })
      .catch(err => {
        console.error('Error fetching data:', err);
        setError(err.message);
        setLoading(false);
      });
  }, []);

  // Calculate KPI values
  const averageHumidity = sensors.length > 0 
    ? sensors.reduce((sum, s) => sum + (s.humedad || 0), 0) / sensors.length
    : 0;
  
  const averageNitrogen = sensors.length > 0 
    ? sensors.reduce((sum, s) => sum + (s.nitrogeno || 0), 0) / sensors.length
    : 0;
  
  const averagePh = sensors.length > 0 
    ? sensors.reduce((sum, s) => sum + (s.ph || 0), 0) / sensors.length
    : 0;
  
  const parcelas = [...new Set(sensors.map(s => s.parcelaId))].filter(Boolean).length;

  // Filter data based on selected filter
  const filteredSensors = filter === 'all' 
    ? sensors 
    : sensors.filter(s => s.parcelaId && s.parcelaId === filter);

  return (
    <div className="space-y-6">
      {/* Page header */}
      <div>
        <h2 className="text-2xl font-bold text-white">Dashboard de Monitoreo</h2>
        <p className="text-agro-dark-400 mt-1">Vista general de las métricas de cultivos</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <KpiCard 
          title="Humedad Promedio" 
          value={`${averageHumidity.toFixed(1)}%`} 
          icon="humidity"
          trend="up"
          trendValue="3.2%"
          color="blue" 
        />
        <KpiCard 
          title="Nitrógeno Promedio" 
          value={averageNitrogen.toFixed(1)}
          icon="nitrogen"
          trend="down"
          trendValue="1.8%"
          color="green" 
        />
        <KpiCard 
          title="pH Promedio" 
          value={averagePh.toFixed(1)}
          icon="ph"
          trend="stable"
          trendValue="0.1"
          color="amber" 
        />
        <KpiCard 
          title="Parcelas Monitoreadas" 
          value={parcelas}
          icon="parcels"
          color="indigo" 
        />
      </div>

      {/* Main content area */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Sensor cards - 2/3 width on lg screens */}
        <div className="lg:col-span-2 space-y-6">
          <div className="bg-agro-dark-900 rounded-xl shadow p-4">
            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-4">
              <h3 className="text-lg font-medium text-white">Lecturas de Sensores</h3>
              
              {/* Filter dropdown - moved here */}
              <div className="flex items-center gap-2">
                <span className="text-sm text-agro-dark-300">Filtrar por parcela:</span>
                <select 
                  className="px-3 py-2 rounded-lg bg-agro-dark-700 border border-agro-dark-600 text-white text-sm focus:outline-none focus:ring-1 focus:ring-agro-green-600"
                  value={filter}
                  onChange={(e) => setFilter(e.target.value)}
                >
                  <option value="all">Todas las parcelas</option>
                  {[...new Set(sensors.map(s => s.parcelaId))].filter(Boolean).map((parcelaId) => (
                    <option key={parcelaId} value={parcelaId}>
                      {parcelaId}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            
            {error && (
              <div className="bg-red-900/20 border border-red-900/30 text-red-400 p-3 rounded-lg mb-4">
                Error: {error}
              </div>
            )}
            
            {loading ? (
              <div className="flex justify-center items-center h-40">
                <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-agro-green-500"></div>
              </div>
            ) : (
              <>
                {filteredSensors.length === 0 ? (
                  <div className="text-center py-10 text-agro-dark-400">
                    No hay datos de sensores disponibles.
                  </div>
                ) : (
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {filteredSensors.map((sensor, index) => (
                      <SensorCard key={sensor.id || `sensor-${index}`} sensor={sensor} />
                    ))}
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* AI Insights - 1/3 width on lg screens */}
        <div className="lg:col-span-1">
          <AiInsights sensors={sensors} />
        </div>
      </div>
    </div>
  );
} 