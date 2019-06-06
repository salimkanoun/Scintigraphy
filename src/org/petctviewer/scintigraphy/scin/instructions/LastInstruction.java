package org.petctviewer.scintigraphy.scin.instructions;

/**
 * This interface is a marker signaling that the instruction is the last one for the controller. After a last
 * instruction, no other instruction can be executed.<br> Every workflow <b>must</b> end with a LastInstruction.
 *
 * @author Titouan QUÉMA
 */
public interface LastInstruction {

}
