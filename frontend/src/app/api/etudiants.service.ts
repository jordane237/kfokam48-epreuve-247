import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApiServiceBase } from './api-service-base';

/** Entrée de la liste de noms (Q1). */
export interface Etudiant {
  id: number;
  nom: string;
}

/** Service dédié à la liste des étudiants (F3). */
@Injectable({ providedIn: 'root' })
export class EtudiantsApiService extends ApiServiceBase {
  private readonly http = inject(HttpClient);

  /** Q1 : la liste dans laquelle l'étudiant choisit son nom. */
  lister(promotionId: number) {
    return this.envelopper(
      this.http.get<Etudiant[]>(`${this.apiUrl}/api/etudiants`, {
        params: { promotionId: String(promotionId) }
      })
    );
  }
}
