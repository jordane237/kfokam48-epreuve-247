import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { EtudiantsApiService, Etudiant as EtudiantListe } from '../api/etudiants.service';
import { PresencesApiService, PresenceCreee } from '../api/presences.service';
import { ExercicesApiService, ExerciceReponse } from '../api/exercices.service';
import { ApiError } from '../api/api-error';

/**
 * Écran Étudiant (F2) : sélection du nom (Q1), marquage de présence avec retour
 * clair sur 400/409/410 et le blocage RG4, dépôt du lien d'exercice (400/409).
 * F3 : états de chargement/erreur gérés partout ; ENF1 : utilisable à 375 px.
 */
@Component({
  selector: 'app-etudiant',
  imports: [FormsModule],
  templateUrl: './etudiant.html',
  styleUrl: './etudiant.css'
})
export class Etudiant implements OnInit {

  private readonly etudiantsApi = inject(EtudiantsApiService);
  private readonly presencesApi = inject(PresencesApiService);
  private readonly exercicesApi = inject(ExercicesApiService);

  promotionId = 1;

  // --- Liste des noms (Q1) ---
  etudiants = signal<EtudiantListe[]>([]);
  listeEnChargement = signal(false);
  erreurListe = signal<ApiError | null>(null);
  etudiantId: number | null = null;

  // --- Présence ---
  code = '';
  presenceEnCours = signal(false);
  erreurPresence = signal<ApiError | null>(null);
  presenceConfirmee = signal<PresenceCreee | null>(null);

  // --- Dépôt ---
  sessionId = 1;
  lien = '';
  depotEnCours = signal(false);
  erreurDepot = signal<ApiError | null>(null);
  exerciceDepose = signal<ExerciceReponse | null>(null);

  ngOnInit(): void {
    this.chargerListe();
  }

  chargerListe(): void {
    this.listeEnChargement.set(true);
    this.erreurListe.set(null);
    this.etudiantsApi.lister(this.promotionId).subscribe({
      next: (liste) => {
        this.etudiants.set(liste);
        this.listeEnChargement.set(false);
      },
      error: (err: ApiError) => {
        this.erreurListe.set(err);
        this.listeEnChargement.set(false);
      }
    });
  }

  marquerPresence(): void {
    if (this.etudiantId === null) {
      this.erreurPresence.set({ code: 'CHAMP_MANQUANT', message: 'Choisissez d\'abord votre nom.' });
      return;
    }
    this.presenceEnCours.set(true);
    this.erreurPresence.set(null);
    this.presenceConfirmee.set(null);
    this.presencesApi.marquer(this.code.trim(), this.etudiantId).subscribe({
      next: (p) => {
        this.presenceConfirmee.set(p);
        this.presenceEnCours.set(false);
        this.code = '';
      },
      error: (err: ApiError) => {
        this.erreurPresence.set(err);
        this.presenceEnCours.set(false);
      }
    });
  }

  deposer(): void {
    if (this.etudiantId === null) {
      this.erreurDepot.set({ code: 'CHAMP_MANQUANT', message: 'Choisissez d\'abord votre nom.' });
      return;
    }
    this.depotEnCours.set(true);
    this.erreurDepot.set(null);
    this.exerciceDepose.set(null);
    this.exercicesApi.deposer(this.sessionId, this.etudiantId, this.lien.trim()).subscribe({
      next: (e) => {
        this.exerciceDepose.set(e);
        this.depotEnCours.set(false);
      },
      error: (err: ApiError) => {
        this.erreurDepot.set(err);
        this.depotEnCours.set(false);
      }
    });
  }
}
