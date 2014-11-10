package com.logica.ndk.tm.utilities.git;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.google.common.collect.ImmutableList;
import com.logica.ndk.tm.utilities.AbstractUtility;
import com.logica.ndk.tm.utilities.ErrorCodes;
import com.logica.ndk.tm.utilities.ResponseStatus;
import com.logica.ndk.tm.utilities.SystemException;

/**
 * @author ondrusekl
 */
public class GitBackupImpl extends AbstractUtility {

  final static List<String> REPO_OBJECTS = ImmutableList.<String> of("branches", "hooks", "logs", "objects", "refs", "config", "HEAD");

  public String execute(final String dirPath, final String gitDirPath, @Nullable String commitMessage) {
    checkNotNull(dirPath, "dirPath must not be null");
    checkArgument(!dirPath.isEmpty(), "dirPath must not be empty");
    checkNotNull(gitDirPath, "gitDirPath must not be null");
    checkArgument(!gitDirPath.isEmpty(), "gitDirPath must not be empty");

    log.info("execute started");

    checkDirectory(dirPath);
    checkGitDirectory(gitDirPath);

    try {
      final FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
      final Repository repository = repositoryBuilder
          .setGitDir(new File(gitDirPath))
          .setWorkTree(new File(dirPath))
          .readEnvironment()
          .build();

      final Git git = new Git(repository);
      Status status = git.status().call();
      log.debug("Untracked files {}", status.getUntracked());

      final AddCommand addCommand = git.add();
      addCommand
          .addFilepattern(".") // add all untracked files and directories in working tree
          .call();

      status = git.status().call();

      log.info("Added files {} for git commit", status.getAdded());

      final CommitCommand commitCommand = git.commit();
      log.debug("Commit added files and dirs into git repo");

      if (commitMessage == null || commitMessage.isEmpty()) {
        commitMessage = format("Zaloha slozky %s (datum: %2$td.%2$tm.%2$tY %2$tT.%2$tL)", dirPath, new Date());
      }
      commitCommand
          .setAuthor("Transformacni modul", "nomail@ndk.cz")
          .setCommitter("Transformacni modul", "nomail@ndk.cz")
          .setMessage(commitMessage)
          .setAll(true)
          .call();
    }
    catch (final Exception e) {
      throw new SystemException("Exception during git backup commands", ErrorCodes.GIT_BACKUP_FAILED);
    }

    log.info("execute finished");
    return ResponseStatus.RESPONSE_OK;
  }

  private void checkGitDirectory(final String gitDirPath) {
    checkNotNull(gitDirPath, "gitDirPath must not be null");

    final File gitDir = new File(gitDirPath);
    if (gitDir.exists()) { // directory exists
      if (gitDir.isDirectory()) { // is directory
        if (gitDir.list().length != 0) { // is not empty
          final List<String> dirContent = ImmutableList.<String> copyOf(gitDir.list());
          if (dirContent.containsAll(REPO_OBJECTS)) { // check repo structure
            log.debug(format("Directory %s looks like initialized GIT repository", gitDirPath));
            return;
          }
        }
      }
      else { // is not directory
        throw new SystemException(format("Path %s is not directory", gitDirPath), ErrorCodes.WRONG_PATH);
      }
    }
    // init git repository
    log.info(format("Initializing git repository in %s", gitDirPath));
    Git.init().setDirectory(gitDir).setBare(true).call();
  }
}
