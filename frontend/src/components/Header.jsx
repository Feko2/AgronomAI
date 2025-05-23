export default function Header() {
  return (
    <header className="h-16 flex items-center justify-between bg-agro-dark-950 border-b border-agro-dark-700 px-6">
      <div className="flex items-center">
        {/* Logo */}
        <div className="h-10 w-10 flex items-center justify-center bg-agro-green-600 rounded-lg">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
          </svg>
        </div>
        <h1 className="text-xl font-bold ml-3 text-agro-green-400">AgroApp</h1>
      </div>

      {/* Welcome message */}
      <div className="text-agro-dark-300">
        ¡Bienvenido al panel de monitoreo de cultivos!
      </div>
    </header>
  );
} 