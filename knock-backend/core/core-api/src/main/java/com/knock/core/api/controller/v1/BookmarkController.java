package com.knock.core.api.controller.v1;

import com.knock.auth.MemberPrincipal;
import com.knock.core.api.controller.v1.response.BookmarkResponseDto;
import com.knock.core.api.controller.v1.response.BookmarkToggleResponseDto;
import com.knock.core.domain.bookmark.BookmarkService;
import com.knock.core.domain.bookmark.dto.BookmarkToggleData;
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
    public ApiResponse<BookmarkToggleResponseDto> toggleBookmarkItem(@AuthenticationPrincipal MemberPrincipal principal, @PathVariable Long itemId) {
        boolean isToggleOn = bookmarkService.toggleBookmark(principal.getMemberId(), new BookmarkToggleData(itemId));
        BookmarkToggleResponseDto response = new BookmarkToggleResponseDto(itemId, isToggleOn);
        return ApiResponse.success(response);
    }

    @GetMapping("/api/v1/items/my-bookmarks")
    public ApiResponse<List<BookmarkResponseDto>> getMyBookmarks(@AuthenticationPrincipal MemberPrincipal principal) {
        return ApiResponse.success(bookmarkService.getMyBookmarks(principal.getMemberId()));
    }

}
