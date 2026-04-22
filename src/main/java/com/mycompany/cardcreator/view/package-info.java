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
 *                      elements and selection state
 *   CardRenderer       paints a CardCanvas to screen and to an export image,
 *                      with a small cache so disk reads don't happen every frame
 *   SoundPlayer        plays UI click and cue sounds from the classpath
 *
 * CardCanvas implements model.CanvasView so the undo records in the
 * model package can tell the view to add, remove, or repaint elements
 * without importing view.* themselves.
 *
 * Rendering belongs to this package; mouse and keyboard handling belongs to
 * the controller package. These screens read from the model package and
 * write to it through Model's methods rather than touching its fields
 * directly.
 */
package com.mycompany.cardcreator.view;
