import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { ApiError } from './api-error';

/**
 * Classe de base de la couche d'appel API (F3) : les services métier
 * (sessions, presences, exercices, relectures, tableau) étendent cette classe
 * et passent tous par `http` — aucun appel dispersé dans les composants.
 */
export abstract class ApiServiceBase {
  protected readonly http = inject(HttpClient);
  protected readonly apiUrl = environment.apiUrl;

  /**
   * Normalise toute erreur vers le format du contrat { code, message }.
   * Si le serveur renvoie déjà ce format, il est conservé tel quel ;
   * sinon (réseau indisponible, erreur inattendue), une erreur générique
   * est produite — jamais de stack trace affichée à l'utilisateur.
   */
  protected extraireErreur(err: HttpErrorResponse): ApiError {
    const corps = err.error as Partial<ApiError> | null;
    if (corps && typeof corps.code === 'string' && typeof corps.message === 'string') {
      return { code: corps.code, message: corps.message };
    }
    if (err.status === 0) {
      return { code: 'SERVEUR_INJOIGNABLE', message: 'Impossible de contacter le serveur.' };
    }
    return { code: 'ERREUR_INATTENDUE', message: `Erreur inattendue (HTTP ${err.status}).` };
  }
}
