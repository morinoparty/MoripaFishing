import { useStaticSearch } from "fumadocs-core/search/client";
import {
    SearchDialog,
    SearchDialogClose,
    SearchDialogContent,
    SearchDialogHeader,
    SearchDialogIcon,
    SearchDialogInput,
    SearchDialogList,
    SearchDialogOverlay,
    type SharedProps,
} from "fumadocs-ui/components/dialog/search";

import { searchIndexUrl } from "@/lib/shared";

// 静的サイトとして配信するため、ビルド時に書き出した検索インデックスをブラウザで読み込んで検索する
export default function DefaultSearchDialog(props: SharedProps) {
    const search = useStaticSearch({ from: searchIndexUrl });

    return (
        <SearchDialog {...search} {...props}>
            <SearchDialogOverlay />
            <SearchDialogContent>
                <SearchDialogHeader>
                    <SearchDialogIcon />
                    <SearchDialogInput />
                    <SearchDialogClose />
                </SearchDialogHeader>
                <SearchDialogList />
            </SearchDialogContent>
        </SearchDialog>
    );
}
