package io.metersphere.metadata.utils;

import com.alibaba.fastjson.JSONObject;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.metadata.vo.repository.GitFileAttachInfo;
import io.metersphere.metadata.vo.repository.RepositoryRequest;
import org.apache.commons.collections.CollectionUtils;
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

import java.util.*;
import java.util.stream.Collectors;

public class GitRepositoryUtils {
    private final String REF_SPACE = "+refs/heads/*:refs/heads/*";
    private final String DEFAULT_GIT_USERNAME = "PRIVATE-TOKEN";

    private String repositoryUrl;
    private String userName;
    private String token;

    private Git git;

    public GitRepositoryUtils(String repositoryUrl, String userName, String token) {
        this.repositoryUrl = StringUtils.trim(repositoryUrl);
        if (StringUtils.isNotBlank(userName)) {
            this.userName = StringUtils.trim(userName);
        } else {
            this.userName = this.DEFAULT_GIT_USERNAME;
        }
        this.token = StringUtils.trim(token);
        LogUtil.info("初始化文件库完成. repositoryUrl：" + repositoryUrl + "; userName：" + userName + "; token：" + token);
    }

    public byte[] getSingleFile(String filePath, String commitId) throws Exception {
        LogUtil.info("准备获取文件. repositoryUrl：" + repositoryUrl + "; filePath：" + filePath + "; commitId：" + commitId);
        InMemoryRepository repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
        ObjectId fileCommitObjectId = repo.resolve(commitId);
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(fileCommitObjectId);
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = new TreeWalk(repo);
        treeWalk.addTree(tree);
        treeWalk.setRecursive(true);
        treeWalk.setFilter(PathFilter.create(filePath));
        if (!treeWalk.next()) {
            LogUtil.info("未获取到文件!. repositoryUrl：" + repositoryUrl + "; filePath：" + filePath + "; commitId：" + commitId);
            return null;
        }
        ObjectId objectId = treeWalk.getObjectId(0);
        ObjectLoader loader = repo.open(objectId);
        byte[] returnBytes = loader.getBytes();
        this.closeConnection(repo);
        return returnBytes;
    }

    public Map<String, byte[]> getFiles(List<RepositoryRequest> repositoryRequestList) throws Exception {
        Map<String, byte[]> returnMap = new HashMap<>();
        if (CollectionUtils.isEmpty(repositoryRequestList)) {
            return returnMap;
        }
        Map<String, List<RepositoryRequest>> commitIdFilePathMap = repositoryRequestList.stream().collect(Collectors.groupingBy(RepositoryRequest::getCommitId));
        LogUtil.info("准备批量获取文件. repositoryUrl：" + repositoryUrl + "; commitIdFilePathMap：" + JSONObject.toJSONString(repositoryRequestList));
        InMemoryRepository repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
        ObjectId fileCommitObjectId = null;
        for (Map.Entry<String, List<RepositoryRequest>> commitFilePathEntry : commitIdFilePathMap.entrySet()) {
            String commitId = commitFilePathEntry.getKey();
            List<RepositoryRequest> itemRequestList = commitFilePathEntry.getValue();
            for (RepositoryRequest repositoryRequest : itemRequestList) {
                String filePath = repositoryRequest.getFilePath();
                fileCommitObjectId = repo.resolve(commitId);
                RevWalk revWalk = new RevWalk(repo);
                RevCommit commit = revWalk.parseCommit(fileCommitObjectId);
                RevTree tree = commit.getTree();
                TreeWalk treeWalk = new TreeWalk(repo);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setFilter(PathFilter.create(filePath));
                if (!treeWalk.next()) {
                    LogUtil.info("未获取到文件!. repositoryUrl：" + repositoryUrl + "; filePath：" + filePath + "; commitId：" + commitId);
                    continue;
                }
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repo.open(objectId);
                returnMap.put(repositoryRequest.getFileMetadataId(), loader.getBytes());
            }
            this.closeConnection(repo);
        }
        LogUtil.info("准备批量获取文件结束. repositoryUrl：" + repositoryUrl);
        return returnMap;
    }

    /**
     * 获取文件的commitId
     *
     * @param branch
     * @return 文件存在，返回对应的commitid
     * @throws Exception 文件不存在的时候报错
     */
    public GitFileAttachInfo selectLastCommitIdByBranch(String branch, String filePath) {
        GitFileAttachInfo attachInfo = null;
        this.validateParams(repositoryUrl, branch, userName, token);
        InMemoryRepository repo = null;
        try {
            repo = this.getGitRepositoryInMemory(repositoryUrl, userName, token);
            ObjectId fileCommitId = repo.resolve("refs/heads/" + branch);
            RevCommit commit = this.getRevTreeByRepositoryAndCommitId(repo, fileCommitId);
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(repo);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            treeWalk.setFilter(PathFilter.create(filePath));
            if (!treeWalk.next()) {
                return null;
            } else {
                ObjectId objectId = treeWalk.getObjectId(0);
                ObjectLoader loader = repo.open(objectId);
                attachInfo = new GitFileAttachInfo(repositoryUrl, userName, token, branch, fileCommitId.getName(), filePath, commit.getFullMessage(), loader.getSize());
                return attachInfo;
            }
        } catch (Exception e) {
            LogUtil.error("获取文件库文件报错!", e);
            MSException.throwException("Connect repository error!");
        } finally {
            this.closeConnection(repo);
        }
        return null;
    }

    private void validateParams(String... params) {
        for (String param : params) {
            if (StringUtils.isBlank(param)) {
                MSException.throwException("Has none params!");
            }
        }
    }

    private RevCommit getRevTreeByRepositoryAndCommitId(InMemoryRepository repo, ObjectId fileCommitId) throws Exception {
        RevWalk revWalk = new RevWalk(repo);
        RevCommit commit = revWalk.parseCommit(fileCommitId);
        return commit;
    }

    private InMemoryRepository getGitRepositoryInMemory(String repositoryUrl, String userName, String token) throws Exception {
        DfsRepositoryDescription repoDesc = new DfsRepositoryDescription();
        InMemoryRepository repo = new InMemoryRepository(repoDesc);
        CredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(userName, token);
        git = new Git(repo);
        git.fetch().setRemote(repositoryUrl).setRefSpecs(new RefSpec(REF_SPACE)).setCredentialsProvider(credentialsProvider).call();
        repo.getObjectDatabase();
        return repo;
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
        this.validateParams(repositoryUrl, userName, token);
        InMemoryRepository repo = null;
        try {
            Collection<Ref> refList;
            UsernamePasswordCredentialsProvider pro = new UsernamePasswordCredentialsProvider(userName, token);
            refList = Git.lsRemoteRepository().setRemote(repositoryUrl).setCredentialsProvider(pro).call();
            refList.forEach(item -> {
                returnList.add(item.getName());
            });
        } catch (Exception e) {
            LogUtil.error("获取文件库文件报错!", e);
        } finally {
            this.closeConnection(repo);
        }
        if (CollectionUtils.isEmpty(returnList)) {
            MSException.throwException("Repository connect error!");
        }
        return returnList;

    }
}
