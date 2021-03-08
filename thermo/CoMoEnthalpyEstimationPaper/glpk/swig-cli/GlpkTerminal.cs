using System;
using System.Collections.Generic;

namespace org.gnu.glpk {

    /**
     * This class manages terminal output.
     * <p>An object can be registered as listener for terminal output using
     * the {@link #addListener(IGlpkTerminalListener) addListener} method.
     * <p>GLPK will call method {@link #callback(string) callback} before producing
     * terminal output. The listeners can inhibit the terminal output by returning
     * <code>false</code> in the {@link IGlpkTerminalListener#output(string) output}
     * routine.
     * <p>If a {@link GlpkException GlpkExeption} has occured it is necessary to
     * call <pre>
     * GLPK.glp_term_hook(null, null);</pre>
     * to reenable listening to terminal output.
     * @see IGlpkTerminalListener
     * @see GlpkException
     * @see GLPK#glp_term_hook(SWIGTYPE_p_f_p_void_p_q_const__char__int,
     * SWIGTYPE_p_void)
     */
    public class GlpkTerminal {

        /**
         * List of callback listeners.
         */
        private static LinkedList<IGlpkTerminalListener> listeners
                = new LinkedList<IGlpkTerminalListener>();

        /**
         * Constructor.
         */
        private GlpkTerminal() {
        }

        /**
         * Callback function called by native library.
         * Output to the console is created if any of the listeners requests it.
         * @param str string to be written to console
         * @return 0 if output is requested
         */
        public static int callback(string str) {
            bool output = false;

            if (listeners.Count > 0) {
                foreach (IGlpkTerminalListener listener in listeners) {
                    output |= listener.output(str);
                }
                if (output) {
                    return 0;
                } else {
                    return 1;
                }
            }
            return 0;
        }
        
        /**
         * Adds a listener for callbacks.
         * @param listener listener for callbacks
         */
        public static void addListener(IGlpkTerminalListener listener) {
            GLPK.glp_term_hook(null, null);
            listeners.AddLast(listener);
        }

        /**
         * Removes first occurance of a listener for callbacks.
         * @param listener listener for callbacks
         * @return true if the listener was found
         */
        public static bool removeListener(IGlpkTerminalListener listener) {
            if (listeners.Contains(listener)) {
                listeners.Remove(listener);
                return true;
            } else {
                return false;
            }
        }
    }
}
