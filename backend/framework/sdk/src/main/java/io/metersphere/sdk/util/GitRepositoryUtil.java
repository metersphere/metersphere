package io.metersphere.sdk.util;

import io.metersphere.sdk.dto.RemoteFileAttachInfo;
import io.metersphere.sdk.dto.RepositoryQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GitRepositoryUtil {

    private final String repositoryUrl;
    private final String userName;
    private final String token;

    private Git git;

    public GitRepositoryUtil(String repositoryUrl, String userName, String token) {
        this.repositoryUrl = StringUtils.trim(repositoryUrl);
        if (StringUtils.isNotBlank(userName)) {
            this.userName = StringUtils.trim(userName);
        } else {
            this.userName = "PRIVATE-TOKEN";
        }
        this.token = StringUtils.trim(token);
    }

    public byte[] getFile(String filePath, String commitId) throws Exception {
        LogUtils.info("准备获取文件. repositoryUrl：" + repositoryUrl + "; filePath：" + filePath + "; commitId：" + commitId);

        InMemoryRepository repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
        ObjectId fileCommitObjectId = repo.resolve(commitId);
        TreeWalk treeWalk = this.getTreeWork(repo, fileCommitObjectId, filePath);
        if (!treeWalk.next()) {
            return null;
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        byte[] returnBytes = loader.getBytes();
        this.closeConnection(repo);
        return returnBytes;
    }

    public InputStream getFileStream(String filePath, String commitId) throws Exception {
        InMemoryRepository repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
        ObjectId fileCommitObjectId = repo.resolve(commitId);
        ObjectId objectId = this.getTreeWork(repo, fileCommitObjectId, filePath).getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        return loader.openStream();
    }

    public Map<String, byte[]> getFiles(List<RepositoryQuery> RepositoryQueryList) throws Exception {
        Map<String, byte[]> returnMap = new HashMap<>();
        if (CollectionUtils.isEmpty(RepositoryQueryList)) {
            return returnMap;
        }
        Map<String, List<RepositoryQuery>> commitIdFilePathMap = RepositoryQueryList.stream().collect(Collectors.groupingBy(RepositoryQuery::getCommitId));
        InMemoryRepository repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
        ObjectId fileCommitObjectId;
        for (Map.Entry<String, List<RepositoryQuery>> commitFilePathEntry : commitIdFilePathMap.entrySet()) {
            String commitId = commitFilePathEntry.getKey();
            List<RepositoryQuery> itemRequestList = commitFilePathEntry.getValue();
            for (RepositoryQuery repositoryQuery : itemRequestList) {
                String filePath = repositoryQuery.getFilePath();
                fileCommitObjectId = repo.resolve(commitId);
                ObjectId objectId = this.getTreeWork(repo, fileCommitObjectId, filePath).getObjectId(0);
                ObjectLoader loader = repo.open(objectId);
                returnMap.put(repositoryQuery.getFileMetadataId(), loader.getBytes());
            }
            this.closeConnection(repo);
        }
        LogUtils.info("准备批量获取文件结束. repositoryUrl：" + repositoryUrl);
        return returnMap;
    }

    public RemoteFileAttachInfo selectLastCommitIdByBranch(String branch, String filePath) {
        InMemoryRepository repo = null;
        TreeWalk treeWalk = null;
        try {
            repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
            ObjectId lastCommitId = repo.resolve("refs/heads/" + branch);
            if (lastCommitId != null) {
                RevCommit commit = this.getRevTreeByRepositoryAndCommitId(repo, lastCommitId);
                RevTree tree = commit.getTree();
                treeWalk = new TreeWalk(repo);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));
                if (!treeWalk.next()) {
                    return null;
                } else {
                    ObjectId objectId = treeWalk.getObjectId(0);
                    ObjectLoader loader = repo.open(objectId);
                    String fileLastCommitId = this.getFileLastCommitId(lastCommitId, filePath);
                    if (StringUtils.isEmpty(fileLastCommitId)) {
                        fileLastCommitId = lastCommitId.getName();
                    }

                    return new RemoteFileAttachInfo(repositoryUrl, userName, token, branch, fileLastCommitId, filePath, this.genCommitMessageWithCommitTime(commit.getFullMessage(), commit.getCommitTime()), loader.getSize());
                }
            }
        } catch (Exception e) {
            LogUtils.error("获取文件库文件报错!", e);
        } finally {
            if (treeWalk != null) {
                treeWalk.close();
            }
            this.closeConnection(repo);
        }
        return new RemoteFileAttachInfo();
    }

    private String genCommitMessageWithCommitTime(String commitMessage, int commitTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return commitMessage + StringUtils.LF + simpleDateFormat.format(new Date(commitTime * 1000L));
    }

    private String getFileLastCommitId(ObjectId objectId, String filePath) throws Exception {
        Iterable<RevCommit> logs = git.log().add(objectId).addPath(filePath).call();
        for (RevCommit rev : logs) {
            return rev.getName();
        }
        return null;
    }

    private RevCommit getRevTreeByRepositoryAndCommitId(InMemoryRepository repo, ObjectId fileCommitId) throws Exception {
        RevWalk revWalk = new RevWalk(repo);
        return revWalk.parseCommit(fileCommitId);
    }

    private InMemoryRepository getGitRepositoryInMemory(String repositoryUrl, String userName, String token) throws Exception {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(userName, token);
        git = new Git(repo);
        String REF_SPACE = "+refs/heads/*:refs/heads/*";
        git.fetch().setRemote(repositoryUrl).setRefSpecs(new RefSpec(REF_SPACE)).setCredentialsProvider(credentialsProvider).call();
        repo.getObjectDatabase();
        return repo;
    }

    private TreeWalk getTreeWork(InMemoryRepository repo, ObjectId fileCommitObjectId, String filePath) throws Exception {
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(fileCommitObjectId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        return treeWalk;
    }

    private void closeConnection(Repository repo) {
        if (git != null) {
            git.close();
        }
        if (repo != null) {
            repo.close();
        }
    }

    public List<String> getBranches() {
        List<String> returnList = new ArrayList<>();
        InMemoryRepository repo = null;
        try {
            Collection<Ref> refList;
            UsernamePasswordCredentialsProvider pro = new UsernamePasswordCredentialsProvider(userName, token);
            refList = Git.lsRemoteRepository().setRemote(repositoryUrl).setCredentialsProvider(pro).call();
            refList.forEach(item -> {
                returnList.add(item.getName());
            });
        } catch (Exception e) {
            LogUtils.error("获取文件库文件报错!", e);
        } finally {
            this.closeConnection(repo);
        }
        return returnList;
    }
}
