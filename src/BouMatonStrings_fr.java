/*
 *  Use classes instead of xxx.properties files to avoid ISO-8859-1
 * 
 *   
 *  BouMaton, a program to compute transformations on pictures.
 *  Copyright (C) 2019  Frédéric Boulanger (frederic.boulanger@centralesupelec.fr)
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.ResourceBundle;

public class BouMatonStrings_fr extends ResourceBundle {
	private HashMap<String, String> strings = new HashMap<String, String>();
	
	public BouMatonStrings_fr() {
		//// Item À propos
		strings.put("AboutMenu", "À propos de BouMaton...");
		//// Menus
		//  Menu Fichier
		strings.put("FileMenu", "Fichier");
		strings.put("OpenItem", "Ouvrir...");
		strings.put("SaveItem", "Enregistrer...");
		strings.put("RevertItem", "Restaurer l'original");
		strings.put("CloseItem", "Fermer");
		strings.put("QuitItem", "Quitter");
		//  Menu Transformations
		strings.put("TransformMenu", "Transformations");
		strings.put("BoulangerItem", "Boulanger");
		strings.put("PhotoMatonItem", "PhotoMaton");
		strings.put("InterruptItem", "Interrompre");
		strings.put("ShowInfoItem", "Afficher les informations");
		strings.put("HideInfoItem", "Masquer les informations");
		strings.put("ShowHistoryItem", "Afficher l'historique");
		strings.put("HideHistoryItem", "Masquer l'historique");
		//// Chaines
		strings.put("NoPictureStr", "Pas d'image");
		strings.put("BoulangerStr", "boulanger");
		strings.put("BoulangerCapStr", "Boulanger");
		strings.put("PhotoMatonStr", "photomaton");
		strings.put("PhotoMatonCapStr", "Photomaton");
		strings.put("SelectFileStr", "Choisissez un fichier");
		strings.put("SaveAsStr", "Enregistrer sous");
		strings.put("ErrorStr", "Erreur ");
		strings.put("DismissStr", "Fermer");
		strings.put("InTimeStr", "en");
		strings.put("PicturePeriodStr", "Période de l'image");
		strings.put("PixelPeriodStr", "Périodes des pixels ");
		strings.put("PixelsStr", "pixels");
		strings.put("PixelsPlaceStr", "pixels en place");
		strings.put("UnknownBuildStr", "<date inconnue>");
		strings.put("BuiltOnStr", "compilé le ");
		strings.put("OKStr", "OK");
		strings.put("CancelStr", "Annuler");
		strings.put("InformationStr", "Informations");
		strings.put("PhotoTransInfoStr", "Informations sur la transformation du photomaton");
		strings.put("BoulangerTransInfoStr", "Informations sur la transformation du boulanger");
		strings.put("CloseStr", "Fermer");
		strings.put("HistoryStr", "Historique");
		strings.put("NoEnoughMemPeriodStr", "Pas assez de mémoire");
		strings.put("NoEnoughMemImageStr", "Pas assez de mémoire");
		strings.put("FreeMemStr", "Mémoire disponible :");
		strings.put("MaxMemStr", "Mémoire maximale :");
		strings.put("JPEGQualityChoiceStr", "Choix de la qualité JPEG");
		strings.put("JPEGQualityStr", "Qualité JPEG :");
		strings.put("CalcJPEGSizeStr", "Calculer la taille");
		strings.put("ByteStr", "octets");
		strings.put("KByteStr", "Ko");
		strings.put("MByteStr", "Mo");
		//// Help
		strings.put("JPEGQualityHelp", " Le format JPEG compresse les images avec une perte de qualité\n0-25: très basse qualité\n  25-50: basse qualité\n  50-75: qualité moyenne\n  75-90: bonne qualité\n  90-100: meilleure qualité");
		strings.put("PNGHelp", " Le format PNG stocke les images sans perte de qualité");
		//// Erreurs
		strings.put("MRJError", "Erreur lors de l'invocation d'une méthode MRJ. Fin du programme.");
		strings.put("JPEGAvailError", "Impossible de trouver l'encodeur JPEG. L'enregistrement des images est impossible.");
		strings.put("CouldNotReadStr", "Impossible de lire");
		strings.put("CouldNotCreateStr", "Impossible de créer");
		strings.put("CouldNotWriteTo", "Impossible d'écrire dans");
		strings.put("CannotSaveAsJPEGError", "Impossible d'enregistrer ce type d'image en JPEG");
		strings.put("JPEGCodecError", "Problème lors de l'invocation de méthode du codec JPEG codec");
		strings.put("NoMemPeriodsError", "Pas assez de mémoire pour calculer la période des transformations");
	}
	
	@Override
	protected Object handleGetObject(String key) {
		return strings.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(strings.keySet());
	}

}
