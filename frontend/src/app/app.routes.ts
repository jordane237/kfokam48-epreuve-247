import { Routes } from '@angular/router';

// Les trois écrans (F2) — chargés à la demande (lazy).
export const routes: Routes = [
  { path: 'formateur', loadComponent: () => import('./formateur/formateur').then(m => m.Formateur) },
  { path: 'etudiant', loadComponent: () => import('./etudiant/etudiant').then(m => m.Etudiant) },
  { path: 'relecteur', loadComponent: () => import('./relecteur/relecteur').then(m => m.Relecteur) },
];
