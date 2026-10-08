(() => {
  "use strict";
  const sidebar = document.getElementById("sidebar");
  document.getElementById("menu-toggle")?.addEventListener("click", (e) => {
    const open = sidebar.classList.toggle("open");
    e.currentTarget.setAttribute("aria-expanded", String(open));
  });
  document.addEventListener("click", (e) => {
    if (
      window.innerWidth < 992 &&
      !sidebar.contains(e.target) &&
      !e.target.closest("#menu-toggle")
    ) {
      sidebar.classList.remove("open");
      document
        .getElementById("menu-toggle")
        ?.setAttribute("aria-expanded", "false");
    }
  });
  let pending = null;
  const modalElement = document.getElementById("confirm-modal");
  const modal = modalElement ? new bootstrap.Modal(modalElement) : null;
  document.addEventListener("submit", (e) => {
    const form = e.target;
    if (
      !form.matches("form[data-confirm]") ||
      form.dataset.confirmed === "true"
    )
      return;
    e.preventDefault();
    pending = form;
    document.getElementById("confirm-message").textContent =
      form.dataset.confirm;
    modal.show();
  });
  document.getElementById("confirm-accept")?.addEventListener("click", () => {
    if (!pending) return;
    pending.dataset.confirmed = "true";
    const form = pending;
    pending = null;
    modal.hide();
    form.requestSubmit();
  });
  document
    .querySelectorAll("[data-print]")
    .forEach((b) => b.addEventListener("click", () => window.print()));

  // Cédula dominicana: conserva únicamente 11 dígitos y muestra la máscara
  // 000-0000000-0 tanto al escribir como al cargar un valor existente.
  function formatCedula(value) {
    const digits = String(value ?? "").replace(/\D/g, "").slice(0, 11);
    if (digits.length <= 3) return digits;
    if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    return `${digits.slice(0, 3)}-${digits.slice(3, 10)}-${digits.slice(10)}`;
  }

  document.querySelectorAll("input[data-cedula-mask]").forEach((input) => {
    input.value = formatCedula(input.value);
  });

  document.addEventListener("input", (e) => {
    const cedula = e.target.closest?.("input[data-cedula-mask]");
    if (cedula) {
      const formatted = formatCedula(cedula.value);
      if (cedula.value !== formatted) cedula.value = formatted;
      return;
    }

    // Otros campos estrictamente numéricos, por ejemplo teléfono.
    const input = e.target.closest?.("input[data-numeric-only]");
    if (!input) return;
    const maxDigits = Number(input.dataset.maxDigits || input.maxLength || 0);
    let sanitized = input.value.replace(/\D/g, "");
    if (maxDigits > 0) sanitized = sanitized.slice(0, maxDigits);
    if (input.value !== sanitized) input.value = sanitized;
  });

  // La máscara es solo visual. Antes de enviar, el backend recibe la cédula
  // normalizada, sin guiones, para mantener una única representación en BD.
  document.addEventListener("submit", (e) => {
    e.target.querySelectorAll?.("input[data-cedula-mask]").forEach((input) => {
      input.value = input.value.replace(/\D/g, "").slice(0, 11);
    });
  });

  const container = document.getElementById("experiences");
  function reindex() {
    container?.querySelectorAll(".experience").forEach((card, index) => {
      card.querySelector("[data-exp-number]").textContent = index + 1;
      card.querySelectorAll("[data-exp-field]").forEach((input) => {
        const field = input.dataset.expField;
        input.name = "experiencias[" + index + "]." + field;
        input.id = "exp-" + index + "-" + field;
        const label = input.parentElement.querySelector("label");
        if (label) label.htmlFor = input.id;
      });
    });
  }
  document.getElementById("add-experience")?.addEventListener("click", () => {
    if (container.children.length >= 30) return;
    const fragment = document
      .getElementById("experience-template")
      .content.cloneNode(true);
    container.appendChild(fragment);
    reindex();
    container.lastElementChild.querySelector("input")?.focus();
  });
  container?.addEventListener("click", (e) => {
    const b = e.target.closest("[data-remove-experience]");
    if (b) {
      b.closest(".experience").remove();
      reindex();
    }
  });
  reindex();
  // Validación de periodos en filas dinámicas y de rangos salariales antes del envío.
  document.addEventListener("input", (e) => {
    const card = e.target.closest(".experience");
    if (card) {
      const start = card.querySelector('[data-exp-field="fechaDesde"]');
      const end = card.querySelector('[data-exp-field="fechaHasta"]');
      if (start && end) end.min = start.value || "";
    }
    const minimum = document.getElementById("salarioMinimo");
    const maximum = document.getElementById("salarioMaximo");
    if (minimum && maximum && e.target === minimum)
      maximum.min = String(Math.max(0.01, Number(minimum.value) || 0));
  });
  document.querySelectorAll("input[data-start-date]").forEach((input) => {
    const start = document.getElementById(input.dataset.startDate);
    function update() {
      if (start?.value) input.min = start.value;
      else input.removeAttribute("min");
    }
    start?.addEventListener("change", update);
    update();
  });
})();
