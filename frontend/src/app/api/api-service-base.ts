import { HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, catchError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiError } from './api-error';

/**
 * Classe de base de la couche d'appel API (F3) : les services métier
 * (sessions, presences, exercices, relectures, tableau) étendent cette classe
 * et passent tous par `http` — aucun appel dispersé dans les composants.
 */
export abstract class ApiServiceBase {
  protected readonly apiUrl = environment.apiUrl;

  /**
   * Enveloppe une requête : normalise toute erreur vers le format du contrat
   * { code, message }. Si le serveur renvoie déjà ce format il est conservé ;
   * sinon (réseau indisponible, erreur inattendue) une erreur générique est
   * produite — jamais de stack trace affichée à l'utilisateur.
   */
  protected envelopper<T>(requete: Observable<T>): Observable<T> {
    return requete.pipe(
      catchError((err: HttpErrorResponse) => throwError(() => this.extraireErreur(err)))
    );
  }

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
