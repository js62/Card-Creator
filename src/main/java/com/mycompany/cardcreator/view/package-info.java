/**
 * Swing panels and windows that show the project to the user.
 *
 * The screens:
 *   ProjectOpener      first window on launch; pick or create a project folder
 *   InstructionsPanel  right-hand help text shown inside the editor
 *   EditorMenuBar      top menu bar with File + Edit options and the
 *                      "last saved" indicator
 *   Toolbox            left-hand panel with add-text, shapes, colors,
 *                      rotation, layer, and image controls
 *   CardCanvas         the actual drawing surface for a card; holds the
 *                      elements, background image, and selection state
 *   CardRenderer       paints a CardCanvas to screen and to an export image,
 *                      with a small cache so disk reads don't happen every frame
 *
 * Rendering belongs to this package; mouse and keyboard handling belongs to
 * the controller package. These screens read from the model package and
 * write to it through Model's methods rather than touching its fields
 * directly.
 */
package com.mycompany.cardcreator.view;
