export const getShortcutKey = (key) => {
  const isMac = typeof window !== "undefined" && window.navigator.platform === "MacIntel";

  const shortcutKeyMap = {
    mod: isMac ? { symbol: "⌘", readable: "Command" } : { symbol: "Ctrl", readable: "Control" },
    alt: isMac ? { symbol: "⌥", readable: "Option" } : { symbol: "Alt", readable: "Alt" },
    shift: { symbol: "⇧", readable: "Shift" },
  };

  return shortcutKeyMap[key.toLowerCase()] || { symbol: key, readable: key };
};

export const getShortcutKeys = (keys) => keys.map(getShortcutKey);

export const getOutput = (editor, format) => {
  switch (format) {
    case "json":
      return editor.getJSON();
    case "html":
      return editor.isEmpty ? "" : editor.getHTML();
    default:
      return editor.getText();
  }
};

export const isUrl = (text, options = { requireHostname: false, allowBase64: false }) => {
  if (text.includes("\n")) return false;

  try {
    const url = new URL(text);
    const blockedProtocols = [
      "javascript:",
      "file:",
      "vbscript:",
      ...(options.allowBase64 ? [] : ["data:"]),
    ];

    if (blockedProtocols.includes(url.protocol)) return false;
    if (options.allowBase64 && url.protocol === "data:")
      return /^data:image\/[a-z]+;base64,/.test(text);
    if (url.hostname) return true;

    return (
        url.protocol !== "" &&
        (url.pathname.startsWith("//") || url.pathname.startsWith("http")) &&
        !options.requireHostname
    );
  } catch {
    return false;
  }
};

export const sanitizeUrl = (url, options = { allowBase64: false }) => {
  if (!url) return undefined;

  if (options.allowBase64 && url.startsWith("data:image")) {
    return isUrl(url, { requireHostname: false, allowBase64: true }) ? url : undefined;
  }

  return (
      isUrl(url, { requireHostname: false, allowBase64: options.allowBase64 }) ||
      /^(\/|#|mailto:|sms:|fax:|tel:)/.test(url)
          ? url
          : `https://${url}`
  );
};

export const randomId = () => Math.random().toString(36).slice(2, 11);
