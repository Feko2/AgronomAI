import { useEffect } from 'react';
import Dashboard from './pages/Dashboard';
import Header from './components/Header';

function App() {
  // Set dark mode by default
  useEffect(() => {
    document.documentElement.classList.add('dark');
  }, []);

  return (
    <div className="flex flex-col h-screen bg-agro-dark-900 text-white">
      <Header />
      <main className="flex-1 overflow-y-auto p-6 bg-agro-dark-800">
        <Dashboard />
      </main>
    </div>
  );
}

export default App; 