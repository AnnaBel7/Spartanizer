package il.org.spartan.plugin;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import org.eclipse.core.commands.*;
import org.eclipse.jdt.core.*;
import org.eclipse.ui.*;
import org.eclipse.ui.progress.*;

import il.org.spartan.spartanizer.dispatch.*;

/** A handler for {@link Spartanizations}. This handler executes all safe
 * Spartanizations on all Java files in the current project.
 * @author Ofir Elmakias <code><elmakias [at] outlook.com></code>
 * @since 2015/08/01 */
public final class SpartanizeAll extends BaseHandler {
  static final int MAX_PASSES = 20;

  /** Returns the number of spartanization tips for a compilation unit
   * @param u JD
   * @return number of tips available for the compilation unit */
  public static int countSuggestions(final ICompilationUnit u) {
    int $ = 0;
    for (final GUI$Applicator ¢ : eclipse.safeSpartanizations) {
      ¢.setMarker(null);
      ¢.setICompilationUnit(u);
      $ += ¢.countSuggestions();
    }
    return $;
  }

  public SpartanizeAll() {
    this(new Trimmer());
  }

  public SpartanizeAll(final GUI$Applicator inner) {
    super(inner);
  }

  @Override public Void execute(@SuppressWarnings("unused") final ExecutionEvent __) throws ExecutionException {
    final StringBuilder message = new StringBuilder();
    final ICompilationUnit currentCompilationUnit = eclipse.currentCompilationUnit();
    final IJavaProject javaProject = currentCompilationUnit.getJavaProject();
    message.append("starting at " + currentCompilationUnit.getElementName() + "\n");
    final List<ICompilationUnit> us = eclipse.facade.compilationUnits(currentCompilationUnit);
    message.append("found " + us.size() + " compilation units \n");
    final IWorkbench wb = PlatformUI.getWorkbench();
    System.out.println("wew");
    final int initialCount = countSuggestions(currentCompilationUnit);
    System.out.println("ewe");
    message.append("with " + initialCount + " tips");
    if (initialCount == 0)
      return eclipse.announce("No tips for '" + javaProject.getElementName() + "' project\n" + message);
    eclipse.announce(message);
    final GUI$Applicator a = new Trimmer();
    for (int i = 0; i < MAX_PASSES; ++i) {
      final IProgressService ps = wb.getProgressService();
      final AtomicInteger passNum = new AtomicInteger(i + 1);
      try {
        ps.run(true, true, pm -> {
          pm.beginTask(
              "Spartanizing project '" + javaProject.getElementName() + "' - " + "Pass " + passNum.get() + " out of maximum of " + MAX_PASSES,
              us.size());
          int n = 0;
          final List<ICompilationUnit> dead = new ArrayList<>();
          for (final ICompilationUnit ¢ : us) {
            if (pm.isCanceled())
              break;
            pm.worked(1);
            pm.subTask(¢.getElementName() + " " + ++n + "/" + us.size());
            if (!a.apply(¢))
              dead.add(¢);
          }
          us.removeAll(dead);
          pm.done();
        });
      } catch (final InvocationTargetException x) {
        LoggingManner.logEvaluationError(this, x);
      } catch (final InterruptedException x) {
        LoggingManner.logEvaluationError(this, x);
      }
      final int finalCount = countSuggestions(currentCompilationUnit);
      return eclipse.announce("Spartanizing '" + javaProject.getElementName() + "' project \n" + "Completed in " + (1 + i) + " passes. \n"
          + "Total changes: " + (initialCount - finalCount) + "\n" + "Suggestions before: " + initialCount + "\n" + "Suggestions after: " + finalCount
          + "\n" + message);
    }
    throw new ExecutionException("Too many iterations");
  }
}
