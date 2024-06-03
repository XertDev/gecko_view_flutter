String extractBaseURLString(String uriString) {
  Uri parsedUri = Uri.parse(uriString);
  String extracted = "";
  if (parsedUri.hasScheme) {
    extracted += "${parsedUri.scheme}://";
  }

  extracted += parsedUri.host;

  if (parsedUri.hasPort) {
    extracted += ":${parsedUri.port}";
  }

  return extracted;
}