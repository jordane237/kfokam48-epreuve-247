import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Sortie de POST/PUT /api/exercices — contrat. */
export interface ExerciceReponse {
  id: number;
  statut: 'depose' | 'en_attente_relecteur' | 'assigne' | 'relu';
}

/** Service dédié aux exercices (F3). */
@Injectable({ providedIn: 'root' })
export class ExercicesApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** EF5 : déposer son exercice ; erreurs 400/409 normalisées. */
  deposer(sessionId: number, etudiantId: number, lien: string) {
    return this.envelopper(
      this.http.post<ExerciceReponse>(`${this.apiUrl}/api/exercices`, { sessionId, etudiantId, lien })
    );
  }

  /** EF6/T12 : remplacer son lien tant que personne n'a relu (RG6). */
  remplacer(exerciceId: number, lien: string) {
    return this.envelopper(
      this.http.put<ExerciceReponse>(`${this.apiUrl}/api/exercices/${exerciceId}`, { lien })
    );
  }
}
