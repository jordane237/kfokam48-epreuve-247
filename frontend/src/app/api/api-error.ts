/**
 * Format d'erreur imposé par le contrat api/contrat.yaml — { code, message }.
 * Toute erreur renvoyée par l'API est normalisée vers cette interface (F3).
 */
export interface ApiError {
  code: string;
  message: string;
}
