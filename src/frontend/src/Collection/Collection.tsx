import {Component, createSignal, onMount, Show} from 'solid-js';
import ItemGrid from '../UI/ItemGrid';
import {CollectionType} from './Collection.type';
import {COLLECTION_API} from '../Api/Api';
import Rest from '../Rest';
import {useParams} from '@solidjs/router';
import {collectionStore, setCollectionStore} from '../Store/CollectionStore';
import {Store} from 'solid-js/store/types/store';
import {setHeaderTitle} from '../Store/HeaderStore';

const Collection: Component = () => {
  const path = useParams<{ readonly collectionId: string; readonly collection: string }>();
  const getCollection = async (): Promise<CollectionType> => {
    const collectionId = Number(path.collectionId);
    if (!isNaN(collectionId)) {
      const response = await Rest({showErrors: true}).get<CollectionType>(COLLECTION_API(collectionId));
      return response.data;
    }
    return Promise.reject(new Error(`book id ${path.collectionId} is not a number!`));
  };

  const [collection, setCollection] = createSignal<Store<CollectionType> | null>(null);

  onMount(() => {
    const collectionName = decodeURIComponent(path.collection);
    setHeaderTitle(collectionName);
    const storedCollection = collectionStore.find(col => col.title.toLowerCase() === collectionName.toLowerCase());
    if (storedCollection === undefined) {
      getCollection()
        .then(r => {
          const responseCollection = {
            ...r,
            books: r.books.sort((book1, book2) => book1.collectionIndex - book2.collectionIndex)
          };
          setCollectionStore([...collectionStore, responseCollection]);
          setCollection(r);
        });
    } else {
      setCollection(storedCollection);
    }
  });

  return (
    <Show when={collection() !== null} fallback={<h1>Loading....</h1>}>
      <ItemGrid
        items={collection()!.books.map((book) => ({
          id: book.id,
          title: book.title,
          itemType: 'book',
          bookCount: 1
        }))}
      />
    </Show>
  );
};

export default Collection;
