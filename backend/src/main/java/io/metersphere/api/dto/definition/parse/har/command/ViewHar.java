///**
// *
// * har - HAR file reader, writer and viewer
// * Copyright (c) 2014, Sandeep Gupta
// *
// * http://sangupta.com/projects/har
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * 		http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// */
//
//package io.metersphere.api.dto.definition.parse.har.command;
//
//import java.io.File;
//
//import com.sangupta.har.HarUtils;
//import com.sangupta.har.model.Har;
//import com.sangupta.har.model.HarEntry;
//import com.sangupta.har.model.HarPage;
//import com.sangupta.jerry.util.AssertUtils;
//
//import io.airlift.command.Arguments;
//import io.airlift.command.Command;
//
//@Command(name = "view", description = "View HAR file")
//public class ViewHar implements Runnable {
//
//	@Arguments
//	private String file;
//
//	@Override
//	public void run() {
//		Har har = null;
//
//		try {
//			har = HarUtils.read(new File(this.file));
//		} catch(Exception e) {
//			System.out.println("Error reading HAR file: " + e.getMessage());
//			return;
//		}
//
//		if(har.log == null || AssertUtils.isEmpty(har.log.pages)) {
//			System.out.println("HAR file has no pages!");
//			return;
//		}
//
//		// connect references
//		HarUtils.connectReferences(har);
//
//		// start displaying
//		System.out.println("Number of pages viewed: " + har.log.pages.size());
//		System.out.println();
//
//		for(HarPage page : har.log.pages) {
//			System.out.println(page);
//
//			// output the calls for this page
//			for(HarEntry entry : page.entries) {
//				System.out.println("\t" + entry);
//			}
//		}
//	}
//
//}
