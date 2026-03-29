package com.arnoagape.lokavelo.ui.screen.bikes.owner.addBike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BikeHelpBottomSheet(
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guide de remplissage",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }

            // Scrollable content
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    HelpSection(
                        emoji = "📸",
                        title = "Photos",
                        items = listOf(
                            "Ajoutez 1 à 3 photos de votre vélo",
                            "Privilégiez une photo de face montrant le vélo dans son ensemble",
                            "Utilisez un bon éclairage naturel",
                            "Évitez les arrière-plans trop chargés",
                            "Les photos nettes et de bonne qualité augmentent les locations"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📝",
                        title = "Titre et description",
                        items = listOf(
                            "Titre : soyez spécifique (ex: 'VTT Trek hardtail 2022' plutôt que 'vélo')",
                            "Décrivez l'état du vélo (excellent, bon, acceptable, à restaurer)",
                            "Mentionnez les équipements (dérailleur, frein, suspension, etc.)",
                            "Décrivez les réparations ou entretiens récents",
                            "Une bonne description augmente la confiance des locataires"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "🏷️",
                        title = "Caractéristiques",
                        items = listOf(
                            "Catégorie : sélectionnez le type de vélo (route, VTT, ville, gravel, etc.)",
                            "Électrique : indiquez si le vélo a une assistance électrique",
                            "Marque : le nom du fabricant du vélo",
                            "Taille : la taille du cadre (S, M, L, XL) ou en cm",
                            "État : soyez honnête sur l'état général du vélo",
                            "Accessoires : freins, dérailleur, lumières, béquille, etc."
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📍",
                        title = "Adresse",
                        items = listOf(
                            "L'adresse exacte ne sera pas visible publiquement",
                            "Elle servira à définir la zone de recherche et vérifier les contacts",
                            "Entrez votre adresse précise pour éviter les problèmes de livraison",
                            "Conseil : être centralement situé peut augmenter les demandes"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "💰",
                        title = "Tarification",
                        items = listOf(
                            "Prix par jour : le tarif de base (obligatoire)",
                            "Vous pouvez proposer des tarifs réduits pour les longues durées",
                            "Prix 2 jours : réduction pour les locations de 2 jours",
                            "Prix semaine : réduction pour les locations d'une semaine",
                            "Prix mois : réduction pour les locations d'un mois",
                            "Conseil : comparez les tarifs de vélos similaires dans votre région"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "🔐",
                        title = "Dépôt de garantie",
                        items = listOf(
                            "Montant bloqué sur la carte du locataire pendant la location",
                            "Recommandé pour protéger votre vélo en cas de dégâts",
                            "Généralement entre 30€ et 100€ selon la valeur du vélo",
                            "Sera remboursé au locataire si le vélo est retourné en bon état",
                            "Les dégâts constatés peuvent réduire le remboursement"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "📅",
                        title = "Durée minimale de location",
                        items = listOf(
                            "Définissez le nombre minimum de jours pour louer votre vélo",
                            "Par défaut : 1 jour",
                            "Si vous préférez les longues locations : 3, 7 ou 30 jours",
                            "Les locataires voient cette information avant de vous contacter",
                            "Conseil : une durée courte permet plus de demandes"
                        )
                    )
                }

                item {
                    HelpSection(
                        emoji = "✅",
                        title = "Avant de publier",
                        items = listOf(
                            "Vérifiez que votre vélo est en bon état de fonctionnement",
                            "Testez les freins, les changements de vitesse, la suspension",
                            "Nettoyez votre vélo avant les photos",
                            "Assurez-vous que tous les champs obligatoires sont remplis",
                            "Relisez les informations pour éviter les erreurs",
                            "Une bonne annonce = plus de locations = plus de revenus"
                        )
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun HelpSection(
    emoji: String,
    title: String,
    items: List<String>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title with emoji
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(end = 12.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
