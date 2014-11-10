package com.logica.ndk.tm.utilities.git;

import static org.fest.assertions.Assertions.assertThat;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.logica.ndk.tm.utilities.AbstractUtilityTest;
import com.logica.ndk.tm.utilities.ResponseStatus;

public class GitBackupImplTest extends AbstractUtilityTest {

  private final GitBackupImpl gitBackup = new GitBackupImpl();

  private String workingTreePath;
  private File workingTree;
  private String gitRepoPath;
  private File gitRepo;
  private final String REPO_NAME = getClass().getSimpleName();
  private Git git;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    workingTreePath = FileUtils.getTempDirectoryPath() + File.separator + REPO_NAME;
    gitRepoPath = FileUtils.getTempDirectoryPath() + File.separator + REPO_NAME + ".git";
    workingTree = new File(workingTreePath);
    workingTree.mkdirs();
    gitRepo = new File(gitRepoPath);
    final FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
    final Repository repository = repositoryBuilder.setGitDir(gitRepo)
        .setWorkTree(workingTree)
        .build();
    git = new Git(repository);
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.deleteDirectory(workingTree);
    FileUtils.deleteDirectory(gitRepo);
  }

  @Test
  public void testBackup() throws Exception {

    gitRepo.mkdirs();

    new File(workingTreePath, "test-file").createNewFile();
    final long gitRepoSize = FileUtils.sizeOfDirectory(gitRepo);

    assertThat(git.status().call().getUntracked()).hasSize(1);

    final String result = gitBackup.execute(workingTreePath, gitRepoPath, null);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(gitRepo)
        .exists()
        .isDirectory();

    final Iterable<RevCommit> log = git.log().call();

    assertThat(log.iterator().hasNext()).isTrue();
    assertThat(git.status().call().getUntracked()).isEmpty();

    assertThat(FileUtils.sizeOfDirectory(gitRepo)).isGreaterThan(gitRepoSize);
  }

  @Test
  public void testBackupNothing() throws Exception {

    gitRepo.mkdirs();

    final long gitRepoSize = FileUtils.sizeOfDirectory(gitRepo);

    final String result = gitBackup.execute(workingTreePath, gitRepoPath, null);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(gitRepo)
        .exists()
        .isDirectory();

    assertThat(git.status().call().getUntracked()).isEmpty();
    assertThat(git.status().call().getModified()).isEmpty();
    assertThat(git.status().call().getChanged()).isEmpty();
    assertThat(FileUtils.sizeOfDirectory(gitRepo)).isGreaterThan(gitRepoSize);
  }

  @Test
  public void testBackupToNotExistsDir() throws Exception {

    new File(workingTreePath, "test-file").createNewFile();

    assertThat(git.status().call().getUntracked()).hasSize(1);
    assertThat(gitRepo).doesNotExist();

    final String result = gitBackup.execute(workingTreePath, gitRepoPath, null);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(gitRepo)
        .exists()
        .isDirectory();

    final Iterable<RevCommit> log = git.log().call();

    assertThat(log.iterator().hasNext()).isTrue();
    assertThat(git.status().call().getUntracked()).isEmpty();
  }

  @Test
  public void testBackupToExistsNotInitializedNotEmptyDir() throws Exception {

    gitRepo.mkdirs();
    new File(gitRepo, "some-file").createNewFile();

    assertThat(gitRepo.list()).isNotEmpty();

    new File(workingTreePath, "test-file").createNewFile();
    final long gitRepoSize = FileUtils.sizeOfDirectory(gitRepo);

    assertThat(git.status().call().getUntracked()).hasSize(1);

    final String result = gitBackup.execute(workingTreePath, gitRepoPath, null);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(gitRepo)
        .exists()
        .isDirectory();

    final Iterable<RevCommit> log = git.log().call();

    assertThat(log.iterator().hasNext()).isTrue();
    assertThat(git.status().call().getUntracked()).isEmpty();

    assertThat(FileUtils.sizeOfDirectory(gitRepo)).isGreaterThan(gitRepoSize);
  }

  @Test
  public void testBackupOnInitializedDir() throws Exception {

    Git.init().setDirectory(gitRepo).setBare(true).call();

    assertThat(gitRepo.list()).contains(GitBackupImpl.REPO_OBJECTS.toArray());

    new File(workingTreePath, "test-file").createNewFile();
    final long gitRepoSize = FileUtils.sizeOfDirectory(gitRepo);

    assertThat(git.status().call().getUntracked()).hasSize(1);

    final String result = gitBackup.execute(workingTreePath, gitRepoPath, null);

    assertThat(result)
        .isNotNull()
        .isEqualTo(ResponseStatus.RESPONSE_OK);

    assertThat(gitRepo)
        .exists()
        .isDirectory();

    final Iterable<RevCommit> log = git.log().call();

    assertThat(log.iterator().hasNext()).isTrue();
    assertThat(git.status().call().getUntracked()).isEmpty();

    assertThat(FileUtils.sizeOfDirectory(gitRepo)).isGreaterThan(gitRepoSize);
  }

}
