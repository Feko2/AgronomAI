export default function SensorCard({ sensor }) {
  // Format the date if it exists
  const formattedDate = sensor.fecha ? 
    new Date(sensor.fecha).toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }) : 'No disponible';

  return (
    <div className="bg-agro-dark-950/70 rounded-lg p-4 border border-agro-dark-800 shadow-sm">
      <div className="flex items-start justify-between">
        <h3 className="font-semibold text-white">
          {sensor.parcelaId || 'Sin ID'}
        </h3>
        <div className="px-2 py-1 rounded-md bg-agro-green-900/40 text-agro-green-400 text-xs">Activo</div>
      </div>
      
      <div className="mt-4 space-y-3">
        <div className="flex justify-between items-center">
          <span className="text-agro-dark-400 text-sm">Humedad:</span>
          <span className="font-mono text-white bg-agro-dark-800 px-2 py-1 rounded text-sm">
            {sensor.humedad !== undefined ? `${sensor.humedad}%` : 'N/A'}
          </span>
        </div>
        
        <div className="flex justify-between items-center">
          <span className="text-agro-dark-400 text-sm">Nitrógeno:</span>
          <span className="font-mono text-white bg-agro-dark-800 px-2 py-1 rounded text-sm">
            {sensor.nitrogeno !== undefined ? sensor.nitrogeno : 'N/A'}
          </span>
        </div>
        
        <div className="flex justify-between items-center">
          <span className="text-agro-dark-400 text-sm">pH:</span>
          <span className="font-mono text-white bg-agro-dark-800 px-2 py-1 rounded text-sm">
            {sensor.ph !== undefined ? sensor.ph : 'N/A'}
          </span>
        </div>
      </div>
      
      <div className="mt-4 pt-3 border-t border-agro-dark-800 flex justify-between">
        <div className="text-xs text-agro-dark-500">Actualizado:</div>
        <div className="text-xs text-agro-dark-400">{formattedDate}</div>
      </div>
    </div>
  );
} 