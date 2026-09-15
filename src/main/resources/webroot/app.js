// Punto 5 — Cliente asíncrono.
// Llama a los servicios con fetch y actualiza SOLO el área de resultado o error,
// sin recargar la página.

const resultBox = document.getElementById("result");
const errorBox  = document.getElementById("error");

function showLoading() {
  errorBox.hidden = true;
  resultBox.textContent = "Cargando…";
}
function showResult(text) {
  errorBox.hidden = true;
  resultBox.textContent = text;
}
function showError(text) {
  resultBox.textContent = "";
  errorBox.hidden = false;
  errorBox.textContent = text;
}

// Hace la petición y separa: fallo de RED vs error HTTP del servidor.
async function callService(url) {
  let res;
  try {
    res = await fetch(url);                      // si no hay red/servidor -> lanza
  } catch (netErr) {
    throw new Error("No se pudo conectar con el servidor (fallo de red).");
  }
  let data = null;
  try { data = await res.json(); } catch (_) { /* la respuesta no era JSON */ }
  if (!res.ok) {                                 // 4xx/5xx: error HTTP controlado
    const msg = (data && data.error) ? data.error : ("Error HTTP " + res.status);
    throw new Error(msg);
  }
  return data;
}

// --- Saludo ---
document.getElementById("form-hello").addEventListener("submit", async (e) => {
  e.preventDefault();                            // evita el envío/recarga por defecto
  const name = document.getElementById("name").value.trim();
  if (!name) { showError("Escribe un nombre."); return; }
  showLoading();
  try {
    const data = await callService("/app/hello?name=" + encodeURIComponent(name));
    showResult(data.greeting);
  } catch (err) { showError(err.message); }
});

// --- Cuadrado ---
document.getElementById("form-square").addEventListener("submit", async (e) => {
  e.preventDefault();
  const raw = document.getElementById("number").value.trim();
  if (raw === "" || isNaN(Number(raw))) { showError("Escribe un número válido."); return; }
  showLoading();
  try {
    const data = await callService("/app/square?x=" + encodeURIComponent(raw));
    showResult(data.input + " al cuadrado = " + data.square);
  } catch (err) { showError(err.message); }
});

// --- Hora del servidor ---
document.getElementById("btn-time").addEventListener("click", async () => {
  showLoading();
  try {
    const data = await callService("/app/time");
    showResult("Hora del servidor: " + data.time);
  } catch (err) { showError(err.message); }
});

// --- Servicio lento (punto 6.2) ---
document.getElementById("btn-slow").addEventListener("click", async () => {
  showLoading();
  try {
    const data = await callService("/app/slow");
    showResult("El servicio lento respondió tras " + data.slept_ms + " ms.");
  } catch (err) { showError(err.message); }
});