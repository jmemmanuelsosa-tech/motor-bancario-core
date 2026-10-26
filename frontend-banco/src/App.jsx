import { useState, useEffect } from "react";

// --- ÍCONOS VECTORIALES CORPORATIVOS (SVG) ---
const IconoBanco = () => (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <polygon points="12 2 2 7 22 7 12 2"></polygon>
    <polyline points="2 17 22 17"></polyline>
    <polyline points="2 22 22 22"></polyline>
    <line x1="6" y1="11" x2="6" y2="17"></line>
    <line x1="10" y1="11" x2="10" y2="17"></line>
    <line x1="14" y1="11" x2="14" y2="17"></line>
    <line x1="18" y1="11" x2="18" y2="17"></line>
  </svg>
);

const IconoUsuarioAvatar = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
    <circle cx="12" cy="7" r="4"></circle>
  </svg>
);

const IconoCandado = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
    <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
  </svg>
);

function App() {
  // Estados de Autenticación y Registro
  const [sesionActiva, setSesionActiva] = useState(null);
  const [modoRegistro, setModoRegistro] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [mensajeAuthError, setMensajeAuthError] = useState("");
  const [mensajeAuthExito, setMensajeAuthExito] = useState("");

  // Estado de Navegación por Solapas ('cuentas' | 'transferencias' | 'auditoria')
  const [solapaActiva, setSolapaActiva] = useState("cuentas");

  // Estados de Negocio Bancario
  const [cuentas, setCuentas] = useState([]);
  const [historial, setHistorial] = useState([]);
  const [cbuOrigen, setCbuOrigen] = useState("0110000000000000000001");
  const [cbuDestino, setCbuDestino] = useState("0070000000000000000002");
  const [monto, setMonto] = useState("");
  const [notificacionExito, setNotificacionExito] = useState("");
  const [notificacionError, setNotificacionError] = useState("");

  // Procesa Login o Registro según la pestaña seleccionada
  const procesarFormularioAuth = async (e) => {
    e.preventDefault();
    setMensajeAuthError("");
    setMensajeAuthExito("");

    const endpoint = modoRegistro
      ? "http://localhost:8080/api/auth/registro"
      : "http://localhost:8080/api/auth/login";

    try {
      const respuesta = await fetch(endpoint, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ username, password }),
      });

      const datos = await respuesta.json();

      if (!respuesta.ok) {
        setMensajeAuthError(datos.error || "Error en la solicitud.");
      } else if (modoRegistro) {
        setMensajeAuthExito(datos.mensaje);
        setModoRegistro(false);
        setPassword("");
      } else {
        setSesionActiva(datos);
      }
    } catch (error) {
      setMensajeAuthError("No se pudo establecer conexión con el servidor central (Puerto 8080).");
    }
  };

  const cerrarSesion = () => {
    setSesionActiva(null);
    setUsername("");
    setPassword("");
    setSolapaActiva("cuentas");
  };

  const cargarDatosBancarios = async () => {
    try {
      const respCuentas = await fetch("http://localhost:8080/api/cuentas");
      setCuentas(await respCuentas.json());

      const respHistorial = await fetch("http://localhost:8080/api/transferencias");
      setHistorial(await respHistorial.json());
    } catch (error) {
      setNotificacionError("Error al sincronizar datos con el servidor.");
    }
  };

  useEffect(() => {
    if (sesionActiva) {
      cargarDatosBancarios();
    }
  }, [sesionActiva]);

  const ejecutarTransferencia = async (e) => {
    e.preventDefault();
    setNotificacionExito("");
    setNotificacionError("");

    const codigoOperacion = "TXN-" + Date.now();

    try {
      const respuesta = await fetch("http://localhost:8080/api/transferencias", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cbuOrigen, cbuDestino, monto, codigoOperacion }),
      });

      const resultado = await respuesta.json();

      if (!respuesta.ok) {
        setNotificacionError(resultado.error);
      } else {
        setNotificacionExito(`Operación confirmada. Comprobante: ${resultado.codigoOperacion}`);
        setMonto("");
        cargarDatosBancarios();
      }
    } catch (error) {
      setNotificacionError("Fallo de red al procesar la transferencia.");
    }
  };

  // PANTALLA 1: LOGIN Y REGISTRO DE USUARIOS CON CONTROL DE BLOQUEO
  if (!sesionActiva) {
    return (
      <div style={estilos.fondoLogin}>
        <div style={estilos.tarjetaLogin}>
          <div style={estilos.cabeceraLogin}>
            <IconoBanco />
            <h1 style={{ fontSize: "19px", margin: "10px 0 0 0", color: "#0f172a", letterSpacing: "0.5px" }}>
              SISTEMA BANCARIO CORE
            </h1>
          </div>

          {/* Selector entre Iniciar Sesión y Crear Usuario */}
          <div style={{ display: "flex", marginTop: "20px", borderBottom: "1px solid #cbd5e1" }}>
            <button
              type="button"
              onClick={() => { setModoRegistro(false); setMensajeAuthError(""); setMensajeAuthExito(""); }}
              style={modoRegistro ? estilos.tabAuthInactiva : estilos.tabAuthActiva}
            >
              Iniciar Sesión
            </button>
            <button
              type="button"
              onClick={() => { setModoRegistro(true); setMensajeAuthError(""); setMensajeAuthExito(""); }}
              style={modoRegistro ? estilos.tabAuthActiva : estilos.tabAuthInactiva}
            >
              Registrar Usuario
            </button>
          </div>

          <form onSubmit={procesarFormularioAuth} style={{ marginTop: "20px" }}>
            <div style={{ marginBottom: "16px" }}>
              <label style={estilos.etiqueta}>Nombre de Usuario</label>
              <input
                type="text"
                placeholder="Ingrese su usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                style={estilos.input}
                required
              />
            </div>

            <div style={{ marginBottom: "20px" }}>
              <label style={estilos.etiqueta}>Clave de Seguridad</label>
              <input
                type="password"
                placeholder="Ingrese su clave"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={estilos.input}
                required
              />
            </div>

            {mensajeAuthError && <div style={estilos.alertaError}>{mensajeAuthError}</div>}
            {mensajeAuthExito && <div style={estilos.alertaExito}>{mensajeAuthExito}</div>}

            <button type="submit" style={estilos.botonPrimario}>
              <IconoCandado />
              <span>{modoRegistro ? "Confirmar Alta de Usuario" : "Ingresar al Panel"}</span>
            </button>
          </form>

          <div style={estilos.pieLogin}>
            <small>Política de Seguridad: La cuenta se bloqueará tras 3 intentos fallidos.</small>
          </div>
        </div>
      </div>
    );
  }

  // PANTALLA 2: PANEL PRINCIPAL DIVIDIDO EN 3 SOLAPAS
  return (
    <div style={estilos.contenedorPrincipal}>
      {/* CABECERA SUPERIOR LIMPIA (Sin subtítulos y con Avatar de Administrador) */}
      <header style={estilos.navbar}>
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <IconoBanco />
          <span style={{ fontWeight: "700", fontSize: "16px", letterSpacing: "0.5px" }}>
            SISTEMA BANCARIO CORE
          </span>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "20px" }}>
          <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
            <div style={estilos.avatarCirculo}>
              <IconoUsuarioAvatar />
            </div>
            <div style={{ textAlign: "left" }}>
              <div style={{ fontWeight: "600", fontSize: "13px", lineHeight: "1.2" }}>Administrador</div>
              <div style={{ fontSize: "11px", color: "#94a3b8" }}>Usuario: {sesionActiva.usuario}</div>
            </div>
          </div>

          <button onClick={cerrarSesion} style={estilos.botonSalir}>
            Cerrar Sesión
          </button>
        </div>
      </header>

      {/* BARRA DE 3 SOLAPAS DE NAVEGACIÓN */}
      <nav style={estilos.barraSolapas}>
        <div style={estilos.contenedorSolapasInterno}>
          <button
            onClick={() => setSolapaActiva("cuentas")}
            style={solapaActiva === "cuentas" ? estilos.solapaActiva : estilos.solapaInactiva}
          >
            Posición de Cuentas
          </button>
          <button
            onClick={() => { setSolapaActiva("transferencias"); setNotificacionError(""); setNotificacionExito(""); }}
            style={solapaActiva === "transferencias" ? estilos.solapaActiva : estilos.solapaInactiva}
          >
            Transferencias Inmediatas
          </button>
          <button
            onClick={() => setSolapaActiva("auditoria")}
            style={solapaActiva === "auditoria" ? estilos.solapaActiva : estilos.solapaInactiva}
          >
            Auditoría Transaccional
          </button>
        </div>
      </nav>

      {/* CONTENIDO SEGÚN LA SOLAPA SELECCIONADA */}
      <main style={estilos.contenido}>
        {/* SOLAPA 1: POSICIÓN DE CUENTAS */}
        {solapaActiva === "cuentas" && (
          <section>
            <h2 style={estilos.tituloSeccion}>Posición Consolidada de Cuentas</h2>
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(320px, 1fr))", gap: "20px" }}>
              {cuentas.map((cuenta) => (
                <div key={cuenta.id} style={estilos.tarjetaCuenta}>
                  <div style={{ display: "flex", justifyContent: "space-between", marginBottom: "14px" }}>
                    <span style={estilos.etiquetaGris}>CAJA DE AHORRO ARS</span>
                    <span style={estilos.badgeVersion}>Versión #{cuenta.version}</span>
                  </div>
                  <div style={{ fontSize: "28px", fontWeight: "700", color: "#0f172a", marginBottom: "14px" }}>
                    $ {cuenta.saldo.toLocaleString("es-AR", { minimumFractionDigits: 2 })}
                  </div>
                  <div style={{ fontSize: "13px", color: "#475569", fontFamily: "monospace" }}>
                    CBU: {cuenta.cbu}
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        {/* SOLAPA 2: TRANSFERENCIAS */}
        {solapaActiva === "transferencias" && (
          <section style={{ maxWidth: "550px" }}>
            <h2 style={estilos.tituloSeccion}>Orden de Transferencia Inmediata</h2>
            <div style={estilos.panelBlanco}>
              <form onSubmit={ejecutarTransferencia}>
                <div style={{ marginBottom: "16px" }}>
                  <label style={estilos.etiqueta}>CBU Cuenta Origen (Débito)</label>
                  <input
                    type="text"
                    value={cbuOrigen}
                    onChange={(e) => setCbuOrigen(e.target.value)}
                    style={estilos.input}
                    required
                  />
                </div>

                <div style={{ marginBottom: "16px" }}>
                  <label style={estilos.etiqueta}>CBU Cuenta Destino (Crédito)</label>
                  <input
                    type="text"
                    value={cbuDestino}
                    onChange={(e) => setCbuDestino(e.target.value)}
                    style={estilos.input}
                    required
                  />
                </div>

                <div style={{ marginBottom: "20px" }}>
                  <label style={estilos.etiqueta}>Importe en Pesos ($)</label>
                  <input
                    type="number"
                    step="0.01"
                    placeholder="0.00"
                    value={monto}
                    onChange={(e) => setMonto(e.target.value)}
                    style={estilos.input}
                    required
                  />
                  <small style={{ display: "block", marginTop: "6px", fontSize: "11px", color: "#64748b" }}>
                    Límite operativo por transacción sin retención preventiva: $500.000,00
                  </small>
                </div>

                {notificacionExito && <div style={estilos.alertaExito}>{notificacionExito}</div>}
                {notificacionError && <div style={estilos.alertaError}>{notificacionError}</div>}

                <button type="submit" style={estilos.botonPrimario}>
                  Confirmar y Ejecutar Transferencia
                </button>
              </form>
            </div>
          </section>
        )}

        {/* SOLAPA 3: AUDITORÍA */}
        {solapaActiva === "auditoria" && (
          <section>
            <h2 style={estilos.tituloSeccion}>Registro de Auditoría Transaccional</h2>
            <div style={estilos.panelBlanco}>
              <table style={estilos.tabla}>
                <thead>
                  <tr style={{ borderBottom: "2px solid #e2e8f0", textAlign: "left", fontSize: "12px", color: "#475569" }}>
                    <th style={{ padding: "12px 10px" }}>ID Operación (Idempotencia)</th>
                    <th style={{ padding: "12px 10px" }}>CBU Origen</th>
                    <th style={{ padding: "12px 10px" }}>CBU Destino</th>
                    <th style={{ padding: "12px 10px", textAlign: "right" }}>Importe</th>
                  </tr>
                </thead>
                <tbody>
                  {historial.length === 0 ? (
                    <tr>
                      <td colSpan="4" style={{ padding: "24px", textAlign: "center", color: "#94a3b8", fontSize: "13px" }}>
                        No existen operaciones registradas en la base de datos.
                      </td>
                    </tr>
                  ) : (
                    historial.map((tx) => (
                      <tr key={tx.id} style={{ borderBottom: "1px solid #f1f5f9", fontSize: "13px" }}>
                        <td style={{ padding: "12px 10px", fontFamily: "monospace", fontWeight: "600", color: "#0f172a" }}>
                          {tx.codigoOperacion}
                        </td>
                        <td style={{ padding: "12px 10px", fontFamily: "monospace", color: "#475569" }}>
                          {tx.cbuOrigen}
                        </td>
                        <td style={{ padding: "12px 10px", fontFamily: "monospace", color: "#475569" }}>
                          {tx.cbuDestino}
                        </td>
                        <td style={{ padding: "12px 10px", textAlign: "right", fontWeight: "700", color: "#0f172a" }}>
                          $ {tx.monto.toLocaleString("es-AR", { minimumFractionDigits: 2 })}
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

const estilos = {
  fondoLogin: {
    minHeight: "100vh",
    backgroundColor: "#0f172a",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    fontFamily: "'Segoe UI', Roboto, sans-serif",
  },
  tarjetaLogin: {
    backgroundColor: "#ffffff",
    width: "100%",
    maxWidth: "400px",
    padding: "32px",
    borderRadius: "6px",
    boxShadow: "0 10px 25px rgba(0,0,0,0.35)",
  },
  cabeceraLogin: {
    textAlign: "center",
    color: "#1e3a8a",
    paddingBottom: "10px",
  },
  tabAuthActiva: {
    flex: 1,
    padding: "10px",
    backgroundColor: "transparent",
    border: "none",
    borderBottom: "2px solid #1e3a8a",
    color: "#1e3a8a",
    fontWeight: "600",
    fontSize: "13px",
    cursor: "pointer",
  },
  tabAuthInactiva: {
    flex: 1,
    padding: "10px",
    backgroundColor: "transparent",
    border: "none",
    color: "#64748b",
    fontSize: "13px",
    cursor: "pointer",
  },
  pieLogin: {
    marginTop: "20px",
    paddingTop: "12px",
    borderTop: "1px solid #f1f5f9",
    textAlign: "center",
    color: "#64748b",
    fontSize: "12px",
  },
  contenedorPrincipal: {
    minHeight: "100vh",
    backgroundColor: "#f1f5f9",
    fontFamily: "'Segoe UI', Roboto, sans-serif",
  },
  navbar: {
    backgroundColor: "#0f172a",
    color: "#ffffff",
    padding: "14px 32px",
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  avatarCirculo: {
    width: "34px",
    height: "34px",
    borderRadius: "50%",
    backgroundColor: "#1e3a8a",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    color: "#ffffff",
    border: "1px solid #3b82f6",
  },
  barraSolapas: {
    backgroundColor: "#ffffff",
    borderBottom: "1px solid #cbd5e1",
    padding: "0 32px",
  },
  contenedorSolapasInterno: {
    maxWidth: "1100px",
    margin: "0 auto",
    display: "flex",
    gap: "24px",
  },
  solapaActiva: {
    padding: "14px 4px",
    backgroundColor: "transparent",
    border: "none",
    borderBottom: "3px solid #1e3a8a",
    color: "#1e3a8a",
    fontWeight: "600",
    fontSize: "14px",
    cursor: "pointer",
  },
  solapaInactiva: {
    padding: "14px 4px",
    backgroundColor: "transparent",
    border: "none",
    borderBottom: "3px solid transparent",
    color: "#64748b",
    fontWeight: "500",
    fontSize: "14px",
    cursor: "pointer",
  },
  contenido: {
    maxWidth: "1100px",
    margin: "0 auto",
    padding: "32px 20px",
  },
  tituloSeccion: {
    fontSize: "18px",
    color: "#1e293b",
    margin: "0 0 20px 0",
    fontWeight: "600",
  },
  tarjetaCuenta: {
    backgroundColor: "#ffffff",
    padding: "24px",
    borderRadius: "6px",
    border: "1px solid #cbd5e1",
    borderLeft: "4px solid #1e3a8a",
  },
  etiquetaGris: {
    fontSize: "11px",
    fontWeight: "700",
    color: "#64748b",
  },
  badgeVersion: {
    fontSize: "11px",
    backgroundColor: "#e2e8f0",
    color: "#334155",
    padding: "2px 8px",
    borderRadius: "4px",
    fontWeight: "600",
  },
  panelBlanco: {
    backgroundColor: "#ffffff",
    padding: "24px",
    borderRadius: "6px",
    border: "1px solid #cbd5e1",
  },
  etiqueta: {
    display: "block",
    fontSize: "12px",
    fontWeight: "600",
    color: "#334155",
    marginBottom: "6px",
  },
  input: {
    width: "100%",
    padding: "10px",
    fontSize: "14px",
    border: "1px solid #cbd5e1",
    borderRadius: "4px",
    boxSizing: "border-box",
  },
  botonPrimario: {
    width: "100%",
    backgroundColor: "#1e3a8a",
    color: "#ffffff",
    border: "none",
    padding: "11px",
    fontSize: "14px",
    fontWeight: "600",
    borderRadius: "4px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    gap: "8px",
  },
  botonSalir: {
    backgroundColor: "transparent",
    color: "#f8fafc",
    border: "1px solid #475569",
    padding: "6px 12px",
    fontSize: "12px",
    borderRadius: "4px",
    cursor: "pointer",
  },
  alertaError: {
    backgroundColor: "#fef2f2",
    borderLeft: "4px solid #dc2626",
    color: "#991b1b",
    padding: "10px 12px",
    fontSize: "13px",
    marginBottom: "16px",
  },
  alertaExito: {
    backgroundColor: "#f0fdf4",
    borderLeft: "4px solid #16a34a",
    color: "#166534",
    padding: "10px 12px",
    fontSize: "13px",
    marginBottom: "16px",
  },
  tabla: {
    width: "100%",
    borderCollapse: "collapse",
  },
};

export default App;