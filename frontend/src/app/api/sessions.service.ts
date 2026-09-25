import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Sortie 201 de POST /api/sessions — contrat. */
export interface SessionCreee {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

/** Service dédié aux sessions — toute la couche HTTP des sessions passe ici (F3). */
@Injectable({ providedIn: 'root' })
export class SessionsApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** EF1 : ouvrir une session, RG1 garantit expirationAt = ouvertureAt + 15 min côté API. */
  ouvrir(titre: string, promotionId: number) {
    return this.envelopper(
      this.http.post<SessionCreee>(`${this.apiUrl}/api/sessions`, { titre, promotionId })
    );
  }
}
