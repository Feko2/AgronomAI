export default function KpiCard({ title, value, icon, trend, trendValue, color = 'green' }) {
  // Icon rendering based on type
  const renderIcon = () => {
    const iconClasses = `h-10 w-10 p-2 rounded-lg ${getColorClass('bg', color, 700, 20)}`;
    const iconColor = getColorClass('text', color, 500);
    
    switch (icon) {
      case 'humidity':
        return (
          <div className={iconClasses}>
            <svg xmlns="http://www.w3.org/2000/svg" className={`h-6 w-6 ${iconColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
            </svg>
          </div>
        );
      case 'nitrogen':
        return (
          <div className={iconClasses}>
            <svg xmlns="http://www.w3.org/2000/svg" className={`h-6 w-6 ${iconColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19.428 15.428a2 2 0 00-1.022-.547l-2.387-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
            </svg>
          </div>
        );
      case 'ph':
        return (
          <div className={iconClasses}>
            <svg xmlns="http://www.w3.org/2000/svg" className={`h-6 w-6 ${iconColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
            </svg>
          </div>
        );
      case 'parcels':
        return (
          <div className={iconClasses}>
            <svg xmlns="http://www.w3.org/2000/svg" className={`h-6 w-6 ${iconColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
            </svg>
          </div>
        );
      default:
        return (
          <div className={iconClasses}>
            <svg xmlns="http://www.w3.org/2000/svg" className={`h-6 w-6 ${iconColor}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
          </div>
        );
    }
  };
  
  // Render trend indicator
  const renderTrend = () => {
    if (!trend) return null;
    
    const trendStyles = {
      up: 'text-green-500',
      down: 'text-red-500',
      stable: 'text-yellow-500',
    };

    const icons = {
      up: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
        </svg>
      ),
      down: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
        </svg>
      ),
      stable: (
        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 12H4" />
        </svg>
      ),
    };
    
    return (
      <div className={`flex items-center ${trendStyles[trend] || ''} text-xs font-medium`}>
        <span className="mr-1">{icons[trend]}</span>
        <span>{trendValue || ''}</span>
      </div>
    );
  };
  
  // Helper function to generate color classes
  function getColorClass(prefix, color, shade, opacity) {
    const opacityString = opacity ? `/${opacity}` : '';
    return `${prefix}-${color}-${shade}${opacityString}`;
  }

  return (
    <div className="bg-agro-dark-900 rounded-xl p-4 shadow">
      <div className="flex justify-between items-start">
        {renderIcon()}
        {trendValue && trend && renderTrend()}
      </div>
      <div className="mt-4">
        <p className="text-agro-dark-300 text-sm">{title}</p>
        <h3 className="text-2xl font-bold text-white mt-1">{value}</h3>
      </div>
    </div>
  );
} 