package io.metersphere.system.file.aspect;

import io.metersphere.sdk.exception.MSException;
import io.metersphere.sdk.util.Translator;
import io.metersphere.system.domain.SystemParameter;
import io.metersphere.system.domain.SystemParameterExample;
import io.metersphere.system.file.annotation.FileLimit;
import io.metersphere.system.mapper.SystemParameterMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
public class FileLimitAspect {

	@Resource
	private SystemParameterMapper systemParameterMapper;

	@Pointcut("@annotation(io.metersphere.system.file.annotation.FileLimit)")
	public void limitPointCut() {

	}

	@Before("limitPointCut()")
	public void before(JoinPoint joinPoint) {
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();
		FileLimit limit = method.getAnnotation(FileLimit.class);
		// 获取方法参数
		Object[] args = joinPoint.getArgs();
		// 获取文件大小限制 (默认使用系统参数中的文件限制大小, 如果注解中有配置则使用注解中的配置)
		long fileMaxSize = limit.maxSize() != 0 ? limit.maxSize() : getFileMaxSize();
		for (Object arg : args) {
			// 单个文件
			if (arg instanceof MultipartFile file) {
				// 判断文件大小是否超过限制
				if (file.getSize() > fileMaxSize * 1024 * 1024) {
					throw new MSException(Translator.get("file_upload.size_limit"));
				}
			}
			// 多个文件
			if (arg instanceof List<?> files) {
				// 非 List<MultipartFile> 类型参数跳过
				if (CollectionUtils.isEmpty(files) || !(files.getFirst() instanceof MultipartFile)) {
					continue;
				}
				files.forEach(f -> {
					if (f instanceof MultipartFile file) {
						// 判断文件大小是否超过限制
						if (file.getSize() > fileMaxSize * 1024 * 1024) {
							throw new MSException(Translator.get("file_upload.size_limit"));
						}
					}
				});
			}
		}
	}

	private Long getFileMaxSize() {
		SystemParameterExample example = new SystemParameterExample();
		example.createCriteria().andParamKeyEqualTo("upload.file.size");
		List<SystemParameter> systemParameters = systemParameterMapper.selectByExample(example);
		return Long.parseLong(systemParameters.getFirst().getParamValue());
	}
}
