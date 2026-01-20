package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.response.BookmarkResponseDto;
import com.knock.core.domain.bookmark.BookmarkService;
import com.knock.core.support.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/api/v1/items/{itemId}/bookmarks")
    public ApiResponse<Void> toggleBookmarkItem(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long itemId) {
        bookmarkService.toggleBookmark(principal.getMemberId(), itemId);
        return ApiResponse.success(null);
    }

    @GetMapping("/api/v1/items/my-bookmarks")
    public ApiResponse<List<BookmarkResponseDto>> getMyBookmarks(@AuthenticationPrincipal MemberPrincipal principal) {
        return ApiResponse.success(bookmarkService.getMyBookmarks(principal.getMemberId()));
    }

}
