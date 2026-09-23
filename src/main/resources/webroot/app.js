const result = document.getElementById("result");

async function showResponse(url) {
  result.textContent = "Loading…";
  try {
    const response = await fetch(url);
    const body = await response.text();
    result.textContent = response.ok ? body : `Request failed (${response.status}): ${body}`;
  } catch (_) {
    result.textContent = "The application server could not be reached.";
  }
}

document.getElementById("greeting-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const name = document.getElementById("name").value.trim();
  showResponse(`/hello?name=${encodeURIComponent(name)}`);
});
document.getElementById("pi-button").addEventListener("click", () => showResponse("/pi"));
